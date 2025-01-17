---
title: Input types
---
An input type `In[A]` defines an input type that produces an `A` or an error.
Input types occur as parameters in queries as a way to let the caller provide arguments to query resolution.

## Scalar
The `Scalar` type defines a terminal input type, and can be any json value.
`Scalar`s act as both input and output types; refer to [output types](./output_types#scalar) for more information on how scalar types work.

## Enum
The `Enum` type defines a mapping from a string to a value (usually a sealed trait) `A`.
More information can be found in the [output types](./output_types#enum) section.

## Arg
The arg type has a couple of uses.
The first and simplest way of using args is for, well, arguments.
The dsl has a smart constructor for arguments that summons the `In[A]` type from the implicit scope, for the argument.
```scala mdoc:silent
import gql.dsl._
import gql.ast._

arg[Int]("superCoolArg")
```
Args can also have default values that can be constructed with the smart constructors from the value dsl `gql.dsl.value`.
```scala mdoc:silent
import gql.dsl.value._

arg[Int]("superCoolArg", scalar(42))
```
And they can be documented.
```scala mdoc:silent
arg[Int]("superCoolArg", scalar(42), "This is a super cool argument")

arg[Int]("superCoolArg", "This is a super cool argument")
```
:::info
Default values are not type-safe, so you can pass any value you want.
The default value will however be checked during schema validation, and again during query evaluation, so you will get an error if you pass a value of the wrong type.

Input objects makes it impossibly difficult to construct a type-safe default value dsl, since input objects might have default values themselves that allow uses of them to only supply a subset of fields.
Consult the [Default values for input objects](./input_types#default-values-for-input-objects) subsection for more information.
:::

Args also have an `Apply` (`Applicative` without pure) instance defined for them:
```scala mdoc:silent
import cats.implicits._

(arg[Int]("arg1"), arg[Int]("arg2", scalar(43))).mapN(_ + _)

arg[Int]("arg1") *> arg[Int]("arg2", scalar(44))
```

Args can naturally be used in field definitions:
```scala mdoc:silent
import cats._
import cats.effect._

final case class Data(str: String)

tpe[IO, Data](
  "Something",
  "field" -> 
    lift(arg[String]("arg1", scalar("default"))){ case (arg1, data) => 
      data.str + arg1 
    }
)
```

## Input
An input consists of a `name` along with some fields.
It turns out that arguments and fields have the same properties and as such, `Arg` is used for fields.
```scala mdoc:silent
final case class InputData(
  name: String,
  age: Int
)

input[InputData](
  "InputData",
  (
    arg[String]("name"),
    arg[Int]("age", scalar(42))
  ).mapN(InputData.apply)
)
```
### Default values for input objects
For input objects however, a default value cannot be properly type checked at compile time, since the default value might be partial.
For instance, cosider the following input type:
```scala mdoc:silent
final case class SomeInput(
  a: Int,
  b: String,
  c: Seq[Int],
  d: Option[Int]
)

implicit lazy val someInput = input[SomeInput](
  "SomeInput",
  (
    arg[Int]("a", scalar(42)),
    arg[String]("b"),
    arg[Seq[Int]]("c", arr(scalar(1), scalar(2), scalar(3))),
    arg[Option[Int]]("d", scalar(42))
  ).mapN(SomeInput.apply)
)
```
Two valid uses of this type could for instance be:
```scala mdoc:silent
arg[SomeInput](
  "someInput1",
  obj(
    "a" -> scalar(42),
    "b" -> scalar("hello1"),
    "c" -> arr(Seq(1, 2, 3).map(scalar(_)): _*)
  )
)

arg[SomeInput](
  "someInput2",
  obj(
    "b" -> scalar("hello2"),
    "d" -> nullValue
  )
)
```

## Input validation
Naturally input can also be validated.
A function `emap` exists on arg, that maps the input to `Either[String, B]` for some `B`.
```scala mdoc:silent
import cats.data._

final case class ValidatedInput(
  a: Int,
  b: NonEmptyList[Int]
)

input[ValidatedInput](
  "ValidatedInput",
  (
    arg[Int]("a", scalar(42), "May not be negative")
      .emap(i => if (i < 0) s"Negative value: $i".asLeft else i.asRight),
      
    arg[Seq[Int]]("b", arr(scalar(1), scalar(2), scalar(3)), "NonEmpty")
      .emap(xs => xs.toList.toNel.toRight("Input is empty.")),
      
  ).mapN(ValidatedInput.apply)
   .emap(v => if (v.a > v.b.combineAll) "a must be larger than the sum of bs".asLeft else v.asRight)
).document("The field `a` must be larger than the sum of `b`.")
```

