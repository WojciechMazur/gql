---
title: Resolvers
---
Resolvers are at the core of gql; a resolver `Resolver[F, I, O]` takes an `I` and produces an `O` in effect `F`.
Resolvers are embedded in fields and act as continuations.
When gql executes a query it first constructs a tree of continueations from your schema and the supplied GraphQL query.

`Resolver`s act and compose like functions with combinators such as `andThen` and `compose`.

Lets start off with some imports:
```scala mdoc
import gql._
import gql.dsl._
import gql.resolver._
import gql.ast._
import cats.effect._
import cats.implicits._
import cats.data._
```

## Resolvers
`Resolver` is a collection of high-level combinators that constructs a tree of `Step`.
:::note
If you are familiar with the relationship between `fs2.Stream` and `fs2.Pull`, then the relationship between `Resolver` and `Step` should be familiar.
:::
### Lift
The simplest `Resolver` type is `lift` which simply lifts a function `I => O` into `Resolver[F, I, O]`.
`lift`'s method form is `map`, which for any resolver `Resolver[F, I, O]` produces a new resolver `Resolver[F, I, O2]` given a function `O => O2`.
```scala mdoc:nest
val r = Resolver.lift[IO, Int](_.toLong)
r.map(_.toString())
```

### LiftF
Another simple resolver is `liftF` which lifts a function `I => F[O]` into `Resolver[F, I, O]`.
`liftF`'s method form is `evalMap`.
```scala mdoc:nest
val r = Resolver.liftF[IO, Int](i => IO(i.toLong))
r.evalMap(l => IO(l.toString()))
```
:::note
`liftF` is a combination of `lift` and then embedding the effect `F` into resolver.
The implementation of `liftF` is a composition of `Step.Alg.Lift` and `Step.Alg.EmbedEffect`.
Most resolvers are implemented or composed on by `lift`ing a function and composing an embedding.
:::

### Arguments
Arguments in gql are provided through resolvers.
A resolver `Resolver[F, I, A]` can be constructed from an argument `Arg[A]`, through either `argument` or `arg` in method form.
```scala mdoc:nest
val ageArg = arg[Int]("age")
val r = Resolver.argument[IO, Nothing, String](arg[String]("name"))
val r2 = r.arg(ageArg)
r2.map{ case (age, name) => s"$name is $age years old" }
```

`Arg` also has an applicative defined for it, so multi-argument resolution can be simplified to:
```scala mdoc:nest
val r = Resolver.argument[IO, Nothing, (String, Int)](
  (arg[String]("name"), arg[Int]("age")).tupled
)
r.map{ case (age, name) => s"$name is $age years old" }
```

### Meta
The `meta` resolver provides metadata regarding query execution, such as the position of query execution, field aliasing and the provided arguments.

### Errors
Well formed errors are returned in an `cats.data.Ior`.

:::info
An `Ior` is a non-exclusive `Either`.
:::

The `Ior` datatype's left side must be `String` and acts as an optional error that will be present in the query result.
gql can return an error and a result for the same path, given that `Ior` has both left and right side defined.

Errors are embedded into resolvers via `rethrow`.
The extension method `rethrow` is present on any resolver of type `Resolver[F, I, Ior[String, O]]`:
```scala mdoc:nest
val r = Resolver.lift[IO, Int](i => Ior.Both("I will be in the errors :)", i))
r.rethrow
```

### First
A `Resolver` also implements `first` (`Resolver[F, A, B] => Resolver[F, (A, C), (B, C)]`) which is very convinient since some `Resolver`s are constant functions (they throw away their arguments/`I`).
Since a `Resolver` does not form a `Monad`, `first` is necessary to implement non-trivial resolver compositions.
For instance, resolving an argument will ignore the input of the resolver, which is not always the desired semantics.

Lets assume we'd like to implement a resolver, that when given a name, can get a list of friends of the person with that name:
```scala mdoc
type PersonId = Int

case class Person(id: PersonId, name: String)

def getFriends(id: PersonId, limit: Int): IO[List[Person]] = ???

def getPerson(name: String): IO[Person] = ???

def getPersonResolver = Resolver.liftF[IO, String](getPerson)

def limitResolver = Resolver.argument[IO, Person, Int](arg[Int]("limit"))

getPersonResolver andThen 
  limitResolver.first[Person].contramap[Person](p => (p, p)) andThen
  Resolver.liftF{ case (limit, p) => getFriends(p.id, limit) }
```
The above example might be a bit tough to follow, but there methods available to make such formulations less gruesome.
```scala mdoc:nest
val limitArg = arg[Int]("limit")
getPersonResolver
  .arg(limitArg)
  .evalMap{ case (limit, p) => getFriends(p.id, limit) }
```

### Batch
Like most other GraphQL implementations, gql also supports batching.

Unlike most other GraphQL implementations, gql's batching implementation features a global query planner that lets gql delay field execution until it can be paired with another field.

Batch declaration and usage occurs as follows:
* Declare a function `Set[K] => F[Map[K, V]]`.
* Give this function to gql and get back a `Resolver[F, Set[K], Map[K, V]]` in a `State` monad (for unique id generation).
* Use this new resolver where you want batching.

And now put into practice:
```scala mdoc
case class Person(id: Int, name: String)

def getPeopleFromDB(ids: Set[Int]): IO[List[Person]] = ???

Resolver.batch[IO, Int, Person]{ keys => 
  getPeopleFromDB(keys).map(_.map(x => x.id -> x).toMap)
}
```

Whenever gql sees this resolver in any composition, it will look for similar resolvers during query planning.

Note, however, that you should only declare each batch resolver variant **once**, that is, you should build your schema in `State`. 
gql consideres different batch instantiations incompatible regardless of any type information.

State has `Monad` (and transitively `Applicative`) defined for it, so it composes well.
Here is an example of multiple batchers:
```scala mdoc
def b1 = Resolver.batch[IO, Int, Person](_ => ???)
def b2 = Resolver.batch[IO, Int, String](_ => ???)

(b1, b2).tupled
```
:::tip
Even if your field doesn't benefit from batching, batching can still do duplicate key elimination.
:::

#### Batch resolver syntax
When a resolver in a very specific form `Resolver[F, Set[K], Map[K, V]]`, then the gql dsl provides some helper methods.
For instance, it is very likely that a batcher is embedded in a singular context (`K => V`).
Here is a showcase of some of the helper methods:
```scala mdoc
def pb: Resolver[IO, Set[Int], Map[Int, Person]] = 
  // Stub implementation
  Resolver.lift(_ => Map.empty)

// None if a key is missing
pb.optionals[List]

// Emits all values
pb.values[List]

// Every key must have an associated value
// or else raise an error via a custom show-like typeclass
implicit lazy val showMissingPersonId =
  ShowMissingKeys.showForKey[Int]("not all people could be found")
pb.force[List]

// Maybe there is one value for one key?
pb.optional

// Same as optional
pb.optionals[cats.Id]

// There is always one value for one key
pb.forceOne

// Same as force but for non-empty containers
pb.forceNE[NonEmptyList]
```

:::tip
For larger programs, consider declaring all your batchers up-front and putting them into some type of collection:
```scala mdoc
case class MyBatchers(
  personBatcher: Resolver[IO, Set[Int], Map[Int, Person]],
  intStringBatcher: Resolver[IO, Set[Int], Map[Int, String]]
)

(b1, b2).mapN(MyBatchers.apply)
```
For most batchers it is likely that you eventually want to pre-compose them in various ways, for instance requsting args, which this pattern promotes.
:::

:::tip
Sometimes you have multiple groups of fields in the same object where each group have different performance overheads.

Say you had a `Person` object in your database.
This `Person` object also exists in a remote api.
This remote api can tell you, the friends of a `Person` given the object's id and name.

```scala mdoc:nest:silent
case class PersonId(id: Int)

def pureFields = fields[IO, PersonId](
  "id" -> lift(_.id)
)

case class PersonDB(
  id: PersonId, 
  name: String, 
  remoteApiId: String
)

// SELECT id, name, remote_api_id FROM person WHERE id in (...)
def dbBatchResolver: Resolver[IO, PersonId, PersonDB] = ???

def dbFields = fields[IO, PersonDB](
  "name" -> lift(_.name),
  "apiId" -> lift(_.remoteApiId)
)

case class PersonRemoteAPI(
  id: PersonId, 
  friends: List[PersonId]
)

// Given a PersonDB we can call the api (via a batched GET or something)
def personBatchResolver: Resolver[IO, PersonDB, PersonRemoteAPI] = ???

def remoteApiFields = fields[IO, PersonRemoteAPI](
  "friends" -> lift(_.friends)
)

// Given a PersonDB object we have the following fields
def dbFields2: Fields[IO, PersonDB] = 
  remoteApiFields.compose(personBatchResolver) ::: dbFields

// Given a PersonId we have every field
// If "friends" is selected, gql will first run `dbBatchResolver` and then `personBatchResolver`
def allFields = dbFields2.compose(dbBatchResolver) ::: pureFields

implicit def person: Type[IO, PersonId] = tpeNel[IO, PersonId](
  "Person",
  allFields
)
```

The general pattern for this decomposition revolves around figuring out what the most basic description of your object is.
In this example, every fields can (eventually through various side-effects) be resolved from just `PersonId`.

Notice that even if we had many more fields, the composition overhead remains constant.
:::

#### Batchers from elsewhere
Most batching implementations have compatible signatures and can be adapted into a gql batcher.

For instance, converting `fetch` to gql:
```scala mdoc:nest
import fetch._

case class PersonId(value: Int)
case class Person(id: PersonId, data: String)

object People extends Data[PersonId, Person] {
  def name = "People"

  def source: DataSource[IO, PersonId, Person] = ???
}

Resolver
  .batch[IO, PersonId, Person](_.toList.toNel.traverse(People.source.batch).map(_.getOrElse(Map.empty)))
```

### Choice
Resolvers also implement `Choice` via `(Resolver[F, A, C], Resolver[F, B, D]) => Resolver[F, Either[A, B], Either[C, D]]`.
On the surface, this combinator may have limited uses, but with a bit of composition we can perform tasks such as caching.

For instance, a combinator derived from `Choice` is `skippable: Resolver[F, I, O] => Resolver[F, Either[I, O], O]`, which acts as a variant of "caching".
If the right side is present we skip the underlying resolver (`Resolver[F, I, O]`) altogether.

More refined variants of this combinator also exist:
```scala mdoc:silent:nest
case class PersonId(id: Int)

case class Person(id: PersonId, data: String)

def getPersonForId: Resolver[IO, PersonId, Person] = ???

type CachedPerson = Either[PersonId, Person]
def cachedPerson = tpe[IO, CachedPerson](
  "Person",
  "id" -> lift(_.map(_.id).merge.id),
  "data" -> build[IO, CachedPerson](_.skipThat(getPersonForId).map(_.data))
)
```

We can also use some of the `compose` tricks from the [batch resolver syntax section](#batch-resolver-syntax) if we have a lot of fields that depend on `Person`. 

:::note
The query planner treats the choice branches as parallel, such that for two instances of a choice, resolvers in the two branches may be batched together.
:::

### Stream
The stream resolver embeds an `fs2.Stream` and provides the ability to emit a stream of results for a graphql subscription.

#### Stream semantics
* When one or more streams emit, the interpreter will re-evaluate the query from the position that emitted.
That is, only the sub-tree that changed will be re-interpreted.
* If two streams emit and one occurs as a child of the other, the child will be ignored since it will be replaced.
* By default, the interpreter will only respect the most-recent emitted data.

This means that by default, gql assumes that your stream should behave like a signal, not sequentially.
However, gql can also adhere sequential semantics.

For instance a schema designed like the following, emits incremental updates regarding the price for some symbol:
```graphql
type PriceChange {
  difference: Float!
}

type Subscription {
  priceChanges(symbolId: ID!): PriceChange!
}
```

And here is a schema that represents an api that emits updates regarding the current price of a symbol:
```graphql
type SymbolState {
  price: Float!
}

type Subscription {
  price(symbolId: ID!): SymbolState!
}
```

Consider the following example where two different evaluation semantics are displayed:
```scala mdoc:silent:nest
case class PriceChange(difference: Float)
def priceChanges(symbolId: String): fs2.Stream[IO, PriceChange] = ???

case class SymbolState(price: Float)
def price(symbolId: String): fs2.Stream[IO, SymbolState] = ???

def priceChangesResolver = Resolver.id[IO, String].sequentialStreamMap(priceChanges)

def priceResolver = Resolver.id[IO, String].streamMap(price)
```

If your stream is sequential, gql will only pull elements when they are needed.

The interpreter performs a global re-interpretation of your schema, when one or more streams emit.
That is, the interpreter cycles through the following two phases:
* Interpret for the current values.
* Await new values (and values that arrived furing the previous step).

Here is an example of some streams in action:
```scala mdoc
import scala.concurrent.duration._
import cats.effect.unsafe.implicits.global

case class Streamed(value: Int)

implicit lazy val streamed: Type[IO, Streamed] = tpe[IO, Streamed](
  "Streamed",
  "value" -> build[IO, Streamed](_.streamMap{ s =>
    fs2.Stream
      .bracket(IO(println(s"allocating $s")))(_ => IO(println(s"releasing $s"))) >>
      fs2.Stream
        .iterate(0)(_ + 1)
        .evalTap(n => IO(println(s"emitting $n for $s")))
        .meteredStartImmediately(((5 - s.value) * 20).millis)
        .as(Streamed(s.value + 1))
  })
)

def query = """
  subscription {
    streamed {
      value {
        value { 
          value {
            __typename
          }
        }
      }
    }
  }
"""

def schema = SchemaShape.make[IO](
  query = tpe[IO, Unit]("Query", "ping" -> lift(_ => "pong")),
  subscription = Some(tpe[IO, Unit]("Subscription", "streamed" -> lift(_ => Streamed(0))))
)

Schema.simple(schema)
  .map(Compiler[IO].compile(_, query))
  .flatMap { case Right(Application.Subscription(stream)) => stream.take(4).compile.drain }
  .unsafeRunSync()
```

gql also allows the user to specify how much time the interpreter may await more stream updates:
```scala mdoc:silent
Schema.simple(schema).map(Compiler[IO].compile(_, query, accumulate=Some(10.millis)))
```

furthermore, gql can also emit interpreter information if you want to look into what gql is doing:
```scala mdoc
Schema.simple(schema)
  .map(Compiler[IO].compile(_, query, debug=gql.interpreter.DebugPrinter[IO](s => IO(println(s)))))
  .flatMap { case Right(Application.Subscription(stream)) => stream.take(3).compile.drain }
  .unsafeRunSync()
```

## Steps
A `Step` is the low-level algebra for a resolver, that describes a single step of evaluation for a query.
The variants of `Step` are clearly listed in the source code. All variants of step provide orthogonal properties.
