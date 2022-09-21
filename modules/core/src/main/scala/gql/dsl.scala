package gql

import cats._
import cats.implicits._
import gql.ast._
import gql.resolver._
import cats.data._

object dsl {
  def tpe[F[_], A](
      name: String,
      hd: (String, Field[F, A, _, _]),
      tl: (String, Field[F, A, _, _])*
  ) = Type[F, A](name, NonEmptyList(hd, tl.toList))

  def arg[A](name: String, default: Option[A] = None)(implicit tpe: In[A]): Arg[A] =
    Arg.initial[A](ArgParam(name, tpe, default))

  def pure[F[_]: Applicative, I, T](resolver: I => T)(implicit tpe: => Out[F, T]): Field[F, I, T, Unit] =
    pure[F, I, T, Unit](Applicative[Arg].unit) { case (i, _) => resolver(i) }(implicitly, tpe)

  def pure[F[_], I, T, A](arg: Arg[A])(resolver: (I, A) => T)(implicit F: Applicative[F], tpe: => Out[F, T]): Field[F, I, T, A] =
    Field[F, I, T, A](
      arg,
      EffectResolver { case (i, a) => F.pure(resolver(i, a).rightIor) },
      Eval.later(tpe)
    )

  def eff[F[_]: Applicative, I, T](resolver: I => F[T])(implicit tpe: => Out[F, T]): Field[F, I, T, Unit] =
    eff[F, I, T, Unit](Applicative[Arg].unit) { case (i, _) => resolver(i) }(implicitly, tpe)

  def eff[F[_], I, T, A](arg: Arg[A])(resolver: (I, A) => F[T])(implicit
      F: Applicative[F],
      tpe: => Out[F, T]
  ): Field[F, I, T, A] =
    Field[F, I, T, A](
      arg,
      EffectResolver { case (i, a) => resolver(i, a).map(_.rightIor) },
      Eval.later(tpe)
    )

  def enum[F[_], A](name: String, hd: (String, A), tl: (String, A)*) = Enum[F, A](name, NonEmptyList(hd, tl.toList))

  final case class PartiallyAppliedInstance[B](val dummy: Boolean = false) extends AnyVal {
    def apply[F[_], A](pf: PartialFunction[A, B])(implicit s: => Selectable[F, B]): Instance[F, A, B] =
      Instance[F, A, B](Eval.later(s))(pf.lift)
  }

  def instance[B]: PartiallyAppliedInstance[B] = PartiallyAppliedInstance[B]()

  def interface[F[_], A](
      name: String,
      hd: (String, Field[F, A, _, _]),
      tl: (String, Field[F, A, _, _])*
  )(
      instanceHd: Instance[F, A, _],
      instanceTl: Instance[F, A, _]*
  ) =
    Interface(
      name,
      instanceHd.asInstanceOf[Instance[F, A, Any]] :: instanceTl.toList.asInstanceOf[List[Instance[F, A, Any]]],
      NonEmptyList(hd, tl.toList)
    )

  def batchFull[F[_]: Functor, I, K, A, T](br: BatcherReference[K, T])(keys: I => F[Ior[String, Set[K]]])(
      reasscociate: (I, Map[K, T]) => F[Ior[String, A]]
  )(implicit tpe: => Out[F, T]) =
    BatchResolver[F, I, K, A, T](br, i => keys(i).map(_.map(xs => Batch(xs, m => IorT(reasscociate(i, m))))))

    // def batchTraverse[F[_], G[_], I, K, T](br: BatcherReference[K, T])(keys: I => F[Ior[String, G[K]]])
}
