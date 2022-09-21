---
title: Resolvers
---
Resolvers are where the most interest should lie, since they act as the layer between input type and next continuation.
The raw resolver types are as expressive as possible to allow as many use cases as possible, which can cause a lot of noise in the daily use of gql.
Therefore the `dsl` should be enough to get started and this section should act as an introduction for the curious.

:::note
The error types have been omitted from the resolver types for brevity.
:::

## EffectResolver
The simplest resolver is the effect resolver `EffectResolver[F, I, A]` which takes `I` to `F[A]`.

## BatchResolver
The batch resolver `BatchResolver[F, I, K, A, T]` allows the interpreter to more effeciently fetch data.
The resolver captures a the following steps in it's type:
 - It takes `I` to `F[Set[K]]`
 - Then merges the keys `Set[K]` from many different resolvers into a single `Set[K]`
 - Then it fetches the values using a user-defined function `Set[K] => F[Map[K, T]]`
 - Finally it maps the values `Map[K, T]` to `F[A]`

:::note
The resolver will automatically construct a GraphQL error if any of the keys are missing.
:::
 
The `BatchResolver` must also have an implementation of `Set[K] => F[Map[K, T]]`, which is referenced by a `BatcherReference[K, T]`.
:::info
The `BatchResolver` cannot directly embed `Set[K] => F[Map[K, T]]`, since this would allow ambiguity.
What if two `BatchResolver`'s were to have their keys merged, what resolver's `Set[K] => F[Map[K, T]]` should be used?
:::

A `BatcherReference[K, T]` is constructed as follows:
```scala
import gql.resolver._
import cats.effect._

val brState = BatcherReference[IO, Int, Int](keys => IO.pure(keys.map(k => k -> k).toMap))
// brState: cats.data.package.State[gql.SchemaState[IO], BatcherReference[Int, Int]] = cats.data.IndexedStateT@5556a9b1
```
A `State` monad is used to keep track of the batchers that have been created and unique id generation.
During schema construction, `State` can be composed using `Monad`ic operations.
`Schema` contains a smart constructor `stateful` that runs the `State` monad.
```scala
import gql._
import gql.dsl._
import cats._

val statefulSchema = brState.map{ br =>
  SchemaShape[Id, Unit](
    tpe(
      "Query",
      "field" -> pure(_ => "placeholder")
      // "field" -> batch(br)(_ => 42)(_.toString())
    )
  )
}
// statefulSchema: data.IndexedStateT[Eval, SchemaState[IO], SchemaState[IO], SchemaShape[Id, Unit]] = cats.data.IndexedStateT@3164e7da
```