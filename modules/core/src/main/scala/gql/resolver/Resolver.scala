/*
 * Copyright 2023 Valdemar Grange
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gql.resolver

import cats.data._
import cats._
import gql._

final class Resolver[+F[_], -I, +O](private[gql] val underlying: Step[F, I, O]) {
  def andThen[F2[x] >: F[x], O2](that: Resolver[F2, O, O2]): Resolver[F2, I, O2] =
    new Resolver(Step.compose(underlying, that.underlying))

  def compose[F2[x] >: F[x], I1 <: I, I2](that: Resolver[F2, I2, I1]): Resolver[F2, I2, O] =
    that andThen this

  def map[O2](f: O => O2): Resolver[F, I, O2] =
    this andThen Resolver.lift(f)

  def evalMap[F2[x] >: F[x], O2](f: O => F2[O2]): Resolver[F2, I, O2] =
    this andThen Resolver.liftF(f)

  def evalContramap[F2[x] >: F[x], I1 <: I, I2](f: I2 => F2[I1]): Resolver[F2, I2, O] =
    Resolver.liftF(f) andThen this

  def fallibleMap[O2](f: O => Ior[String, O2]): Resolver[F, I, O2] =
    this.map(f) andThen (new Resolver(Step.embedError))

  def first[C]: Resolver[F, (I, C), (O, C)] =
    new Resolver(Step.first(underlying))

  def arg[A](arg: Arg[A]): Resolver[F, I, (A, O)] =
    this andThen Resolver.argument[F, O, A](arg).tupleIn

  def contraArg[A, I2](arg: Arg[A])(implicit ev: (A, I2) <:< I): Resolver[F, I2, O] =
    Resolver.id[F, I2].arg(arg) andThen this.contramap(ev.apply)

  def meta: Resolver[F, I, (Meta, O)] =
    this andThen Resolver.meta[F, O].tupleIn

  def streamMap[F2[x] >: F[x], O2](f: O => fs2.Stream[F2, O2]): Resolver[F2, I, O2] =
    this.map(f).embedStream

  def sequentialStreamMap[F2[x] >: F[x], O2](f: O => fs2.Stream[F2, O2]): Resolver[F2, I, O2] =
    this.map(f).embedSequentialStream
}

object Resolver extends ResolverInstances {
  def liftFull[F[_], I, O](f: I => O): Resolver[F, I, O] =
    new Resolver(Step.lift(f))

  final class PartiallyAppliedLift[F[_], I](private val dummy: Boolean = true) extends AnyVal {
    def apply[O](f: I => O): Resolver[F, I, O] = liftFull(f)
  }

  def lift[F[_], I]: PartiallyAppliedLift[F, I] = new PartiallyAppliedLift[F, I]

  def id[F[_], I]: Resolver[F, I, I] =
    lift(identity)

  def liftFullF[F[_], I, O](f: I => F[O]): Resolver[F, I, O] =
    liftFull(f).andThen(new Resolver(Step.embedEffect))

  final class PartiallAppliedLiftF[F[_], I](private val dummy: Boolean = true) extends AnyVal {
    def apply[O](f: I => F[O]): Resolver[F, I, O] = liftFullF(f)
  }

  def liftF[F[_], I]: PartiallAppliedLiftF[F, I] = new PartiallAppliedLiftF[F, I]

  def argument[F[_], I <: Any, A](arg: Arg[A]): Resolver[F, I, A] =
    new Resolver(Step.argument(arg))

  def meta[F[_], I <: Any]: Resolver[F, I, Meta] =
    new Resolver(Step.getMeta)

  def streamFull[F[_], I, O](f: I => fs2.Stream[F, O]): Resolver[F, I, O] =
    liftFull(f).andThen(new Resolver(Step.embedStream))

  final class PartiallyAppliedStream[F[_], I](private val dummy: Boolean = true) extends AnyVal {
    def apply[O](f: I => fs2.Stream[F, O]): Resolver[F, I, O] = streamFull(f)
  }

  def stream[F[_], I]: PartiallyAppliedStream[F, I] = new PartiallyAppliedStream[F, I]

  def batch[F[_], K, V](f: Set[K] => F[Map[K, V]]): State[gql.SchemaState[F], Resolver[F, Set[K], Map[K, V]]] =
    Step.batch[F, K, V](f).map(new Resolver(_))

  implicit class RethrowOps[F[_], I, O](private val self: Resolver[F, I, Ior[String, O]]) extends AnyVal {
    def rethrow: Resolver[F, I, O] =
      self andThen (new Resolver(Step.embedError))
  }

  implicit class InvariantOps[F[_], I, O](private val self: Resolver[F, I, O]) extends AnyVal {
    def choose[I2, O2](that: Resolver[F, I2, O2]): Resolver[F, Either[I, I2], Either[O, O2]] =
      new Resolver(Step.choose(self.underlying, that.underlying))

    def choice[I2](that: Resolver[F, I2, O]): Resolver[F, Either[I, I2], O] =
      choose[I2, O](that).map(_.merge)

    def skippable: Resolver[F, Either[I, O], O] =
      this.choice(Resolver.id[F, O])

    def skipThis[I2](verify: Resolver[F, I2, Either[I, O]]): Resolver[F, I2, O] =
      verify andThen self.skippable

    def skipThisWith[I2](f: Resolver[F, I2, I2] => Resolver[F, I2, Either[I, O]]): Resolver[F, I2, O] =
      skipThis[I2](f(Resolver.id[F, I2]))

    def continue[O2](f: Resolver[F, O, O] => Resolver[F, O, O2]): Resolver[F, I, O2] =
      self andThen f(Resolver.id[F, O])

    def contramap[I2](f: I2 => I): Resolver[F, I2, O] =
      Resolver.lift(f) andThen self

    def evalContramap[I2](f: I2 => F[I]): Resolver[F, I2, O] =
      Resolver.liftF(f) andThen self

    def fallibleContraMap[I2](f: I2 => Ior[String, I]): Resolver[F, I2, O] =
      (new Resolver(Step.embedError[F, I])).contramap[I2](f) andThen self

    def tupleIn: Resolver[F, I, (O, I)] =
      self.first[I].contramap[I](i => (i, i))
  }

  implicit class SkipThatInvariantOps[F[_], I, I2, O](private val self: Resolver[F, I, Either[I2, O]]) extends AnyVal {
    def skipThat(compute: Resolver[F, I2, O]): Resolver[F, I, O] =
      compute skipThis self

    def skipThatWith(f: Resolver[F, I2, I2] => Resolver[F, I2, O]): Resolver[F, I, O] =
      skipThat(f(Resolver.id[F, I2]))
  }

  implicit class StreamOps[F[_], I, O](private val self: Resolver[F, I, fs2.Stream[F, O]]) extends AnyVal {
    def embedStream: Resolver[F, I, O] =
      self andThen new Resolver(Step.embedStream)

    def embedSequentialStream: Resolver[F, I, O] =
      self andThen new Resolver(Step.embedStreamFull(signal = false))
  }
}

trait ResolverInstances {
  import cats.arrow._
  implicit def arrowChoiceForResolver[F[_]]: ArrowChoice[Resolver[F, *, *]] = new ArrowChoice[Resolver[F, *, *]] {
    override def choose[A, B, C, D](f: Resolver[F, A, C])(g: Resolver[F, B, D]): Resolver[F, Either[A, B], Either[C, D]] =
      f.choose(g)

    override def compose[A, B, C](f: Resolver[F, B, C], g: Resolver[F, A, B]): Resolver[F, A, C] =
      f.compose(g)

    override def first[A, B, C](fa: Resolver[F, A, B]): Resolver[F, (A, C), (B, C)] =
      fa.first[C]

    override def lift[A, B](f: A => B): Resolver[F, A, B] = Resolver.lift(f)
  }

  implicit def applicativeForResolver[F[_], I]: Applicative[Resolver[F, I, *]] = new Applicative[Resolver[F, I, *]] {
    override def ap[A, B](ff: Resolver[F, I, A => B])(fa: Resolver[F, I, A]): Resolver[F, I, B] =
      ff.tupleIn andThen
        fa
          .contramap[(A => B, I)] { case (_, i) => i }
          .tupleIn
          .map { case (a, (f, _)) => f(a) }

    override def pure[A](x: A): Resolver[F, I, A] =
      Resolver.lift(_ => x)
  }
}
