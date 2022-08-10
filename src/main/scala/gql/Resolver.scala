package gql

import cats._
import cats.implicits._
import cats.effect._

sealed trait Resolver[F[_], I, A] {
  def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Resolver[G, I, A]

  def contramap[B](g: B => I): Resolver[F, B, A]
}

object Resolver {
  sealed trait LeafResolver[F[_], I, A] extends Resolver[F, I, A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): LeafResolver[G, I, A]

    override def contramap[B](g: B => I): LeafResolver[F, B, A]
  }

  /*
   * Signal
   * Simplest type would be input => stream of output value
   *
   * But what if we'd like to include some kind of batching? what does that even mean?
   *
   * Naively every output in the stream triggers a *unique* re-evaluation of the subtree
   * Consequently this is not batchable, even though there may be multiple nodes that react to the same data.
   * 
   * Some choices can be made:
   *
   * Do we require the user to "push" the signal up to the common ancestor?
   *
   * Maybe we can generate a key from the input that tells the interpreter what streams are equivalent,
   * which in turn can be used to group updates?
   * This seems like it would be a lot of trouble to both implement and reason with.
   *
   * How do we track these updates? Maybe we have a map Map[K, List[StreamData]] 
   * where StreamData is the field information and such.
   * Then we must await the change in all List[StreamData].size elements.
   * There is a lot of concurrency problems involved.
   * For instance, what happens if two nodes get the same element concurrently?
   * Maybe we have a map Map[K, Int] of generations of K? such that we can zipWithIndex and only cause updates to generations
   * older than our index?
   *
   * Maybe we assume that the streams are the same, thus they share the subscription?
   * Using this model, we must decompose the (potential) effect performed by the stream such that we can avoid 
   * the N+1 problem (every instance of the same stream performing the same effect for every element).
   *
   * This might also be solvable with just a cache?
   * One might use some data generation technique and query the cache with type Map[K, F[A]] where F[A] 
   * is some uncompleted promise.
   * I think this caching solution is the most feasible.
   *
   * Batching different keys (say, within a time period or as a debounce) can be implemented by the end-user.
   * For instance, the end-user can provide some state shared by the whole subscription query that 
   * uses some structure to block streams until the timeout has elapsed.
   *
   */
  final case class Signal[F[_]: MonadCancelThrow, I, A](
      head: LeafResolver[F, I, A],
      tail: Resource[F, (A, I) => fs2.Stream[F, A]],
      key: (A, I) => String
  ) extends Resolver[F, I, A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Resolver[G, I, A] =
      Signal(head.mapK(fk), tail.mapK(fk).map(f => (a, i) => f(a, i).translate(fk)))

    override def contramap[B](g: B => I): Resolver[F, B, A] =
      Signal(head.contramap(g), tail.map(f => (a, i) => f(a, g(i))))
  }

  final case class Pure[F[_], I, A](resolve: I => A) extends LeafResolver[F, I, A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Pure[G, I, A] =
      Pure(resolve)

    override def contramap[B](g: B => I): Pure[F, B, A] =
      Pure(g andThen resolve)
  }

  final case class Effect[F[_], I, A](resolve: I => F[A]) extends LeafResolver[F, I, A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Effect[G, I, A] =
      Effect(resolve.andThen(fk.apply))

    override def contramap[B](g: B => I): Effect[F, B, A] =
      Effect(g andThen resolve)
  }

  final case class Batcher[F[_], K, T](
      batchName: String,
      resolver: Set[K] => F[Map[K, T]]
  ) {
    def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Batcher[G, K, T] =
      Batcher(batchName, resolver.andThen(fk.apply))
  }

  final case class Batch[F[_], K, A, T](
      keys: List[K],
      post: List[(K, T)] => F[A]
  ) {
    def mapK[G[_]](fk: F ~> G): Batch[G, K, A, T] =
      Batch(keys, post.andThen(fk.apply))
  }

  object Batch {
    implicit def applyForBatch[F[_]: Applicative, K, T] = {
      type G[A] = Batch[F, K, A, T]
      new Applicative[G] {
        override def pure[A](x: A): G[A] = Batch(List.empty, _ => x.pure[F])

        override def ap[A, B](ff: G[A => B])(fa: G[A]): G[B] =
          Batch(
            ff.keys ++ fa.keys,
            { m =>
              val f = ff.post(m.take(ff.keys.size))
              val a = fa.post(m.drop(ff.keys.size))
              f.ap(a)
            }
          )
      }
    }
  }

  final case class Batched[F[_], I, K, A, T](
      batch: I => F[Batch[F, K, A, T]],
      batcher: Batcher[F, K, T]
  ) extends LeafResolver[F, I, A] {
    override def mapK[G[_]: MonadCancelThrow](fk: F ~> G): Batched[G, I, K, A, T] =
      Batched(batch.andThen(fa => fk(fa).map(_.mapK(fk))), batcher.mapK(fk))

    override def contramap[B](g: B => I): Batched[F, B, K, A, T] =
      Batched(g.andThen(batch), batcher)
  }
}
