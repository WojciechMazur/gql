package gql

import cats.implicits._
import io.circe._
import cats._
import cats.effect._
import cats.data._
import gql.resolver._
import gql.Value._

object ast extends AstImplicits.Implicits {
  sealed trait Out[F[_], +A] {
    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Out[G, A]
  }

  sealed trait In[+A]

  sealed trait Toplevel[+A] {
    def name: String
  }

  sealed trait OutToplevel[F[_], +A] extends Out[F, A] with Toplevel[A]

  sealed trait InToplevel[+A] extends In[A] with Toplevel[A]

  sealed trait Selectable[F[_], A] extends OutToplevel[F, A] {
    def fieldsList: List[(String, Field[F, A, _, _])]

    def fieldMap: Map[String, Field[F, A, _, _]]

    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Selectable[G, A]

    def contramap[B](f: B => A): Out[F, B]
  }

  final case class Type[F[_], A](
      name: String,
      fields: NonEmptyList[(String, Field[F, A, _, _])]
  ) extends Selectable[F, A] {
    lazy val fieldsList: List[(String, Field[F, A, _, _])] = fields.toList

    override def contramap[B](f: B => A): Type[F, B] =
      Type(name, fields.map { case (k, v) => k -> v.contramap(f) })

    lazy val fieldMap = fields.toNem.toSortedMap.toMap

    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Type[G, A] =
      Type(name, fields.map { case (k, v) => k -> v.mapK(fk) })
  }

  final case class Input[A](name: String, fields: Arg[A]) extends InToplevel[A]

  final case class Union[F[_], A](
      name: String,
      types: NonEmptyList[Instance[F, A, Any]]
  ) extends Selectable[F, A] {
    override def contramap[B](f: B => A): Union[F, B] =
      Union(name, types.map(_.contramap(f)))

    lazy val instanceMap = types.map(i => i.ol.name -> i).toList.toMap

    lazy val fieldMap = Map.empty

    lazy val fieldsList: List[(String, Field[F, A, _, _])] = Nil

    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Union[G, A] =
      Union(
        name,
        types.map(_.mapK(fk))
      )
  }

  final case class Interface[F[_], A](
      name: String,
      instances: List[Instance[F, A, Any]],
      fields: NonEmptyList[(String, Field[F, A, _, _])]
  ) extends Selectable[F, A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Interface[G, A] =
      copy[G, A](
        instances = instances.map(_.mapK(fk)),
        fields = fields.map { case (k, v) => k -> v.mapK(fk) }
      )

    lazy val fieldsList = fields.toList

    lazy val fieldMap = fields.toNem.toSortedMap.toMap

    lazy val instanceMap = instances.map(x => x.ol.name -> x).toMap

    def contramap[B](g: B => A): Interface[F, B] =
      Interface(
        name,
        instances.map(_.contramap(g)),
        fields.map { case (k, v) => k -> v.contramap(g) }
      )
  }

  final case class Scalar[F[_], A](name: String, codec: Codec[A]) extends OutToplevel[F, A] with In[A] with InToplevel[A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Scalar[G, A] =
      Scalar(name, codec)
  }

  final case class Enum[F[_], A](name: String, mappings: NonEmptyList[(String, A)]) extends OutToplevel[F, A] with InToplevel[A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Out[G, A] =
      Enum(name, mappings)

    lazy val m = mappings.toNem

    lazy val revm = mappings.map(_.swap).toList.toMap
  }

  final case class Field[F[_], -I, T, A](
      args: Arg[A],
      resolve: Resolver[F, (I, A), T],
      output: Eval[Out[F, T]]
  ) {
    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Field[G, I, T, A] =
      Field[G, I, T, A](
        args,
        resolve.mapK(fk),
        output.map(_.mapK(fk))
      )

    def contramap[B](g: B => I): Field[F, B, T, A] =
      Field(
        args,
        resolve.contramap[(B, A)] { case (b, a) => (g(b), a) },
        output
      )
  }

  final case class Instance[F[_], A, B](ol: Selectable[F, B])(implicit val specify: A => Option[B]) {
    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Instance[G, A, B] =
      Instance(ol.mapK(fk))

    def contramap[C](g: C => A): Instance[F, C, B] =
      Instance[F, C, B](ol)(c => specify(g(c)))
  }

  final case class OutOpt[F[_], A](of: Out[F, A]) extends Out[F, Option[A]] {
    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): OutOpt[G, A] = OutOpt(of.mapK(fk))
  }

  final case class OutArr[F[_], A, C[x] <: Seq[x]](of: Out[F, A]) extends Out[F, C[A]] {
    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): OutArr[G, A, C] = OutArr(of.mapK(fk))
  }

  final case class InOpt[A](of: In[A]) extends In[Option[A]]

  final case class InArr[A, G[_] <: Seq[_]](of: In[A]) extends In[G[A]]

  final case class ID[+A](value: A) extends AnyVal
  implicit def idTpe[F[_], A](implicit s: Scalar[F, A]): In[ID[A]] =
    Scalar("ID", s.codec.imap(ID(_))(_.value))

  object Scalar {
    def fromCirce[F[_], A](name: String)(implicit enc: Encoder[A], dec: Decoder[A]): Scalar[F, A] =
      Scalar(name, Codec.from(dec, enc))
  }
}

object AstImplicits {
  import ast._

  trait Implicits extends LowPriorityImplicits {
    implicit def stringScalar[F[_]]: Scalar[F, String] = Scalar.fromCirce[F, String]("String")
    implicit def intScalar[F[_]]: Scalar[F, Int] = Scalar.fromCirce[F, Int]("Int")
    implicit def longScalar[F[_]]: Scalar[F, Long] = Scalar.fromCirce[F, Long]("Long")
    implicit def floatScalar[F[_]]: Scalar[F, Float] = Scalar.fromCirce[F, Float]("Float")
    implicit def doubleScalar[F[_]]: Scalar[F, Double] = Scalar.fromCirce[F, Double]("Double")
    implicit def bigIntScalar[F[_]]: Scalar[F, BigInt] = Scalar.fromCirce[F, BigInt]("BigInt")
    implicit def bigDecimalScalar[F[_]]: Scalar[F, BigDecimal] = Scalar.fromCirce[F, BigDecimal]("BigDecimal")
    implicit def booleanScalar[F[_]]: Scalar[F, Boolean] = Scalar.fromCirce[F, Boolean]("Boolean")

    implicit def gqlOutOption[F[_], A](implicit tpe: Out[F, A]): Out[F, Option[A]] = OutOpt(tpe)

    implicit def gqlInOption[A](implicit tpe: In[A]): In[Option[A]] = InOpt(tpe)
  }

  trait LowPriorityImplicits {
    implicit def gqlOutSeq[F[_], A, G[_] <: Seq[_]](implicit tpe: Out[F, A]): Out[F, G[A]] = OutArr(tpe)
    implicit def gqlSeq[A, G[_] <: Seq[_]](implicit tpe: In[A]): In[G[A]] = InArr(tpe)
  }
}
