package gql

import cats.effect.implicits._
import scala.concurrent.duration._
import cats.data._
import cats.implicits._
import cats.effect._
import io.circe._
import cats._
import cats.arrow.FunctionK
import cats.effect.unsafe.implicits.global
import gql.parser.QueryParser
import gql.parser.QueryParser.Value._
import conversions._
import cats.effect.std.Random
import cats.parse.Parser
import cats.parse.Parser.Expectation._
import scala.io.AnsiColor
import scala.concurrent.ExecutionContext
import alleycats.Empty
import gql.resolver._
import cats.mtl._
import cats.instances.unit
import gql.parser.ParserUtil
import gql.ast._
import fs2.Pull
import fs2.concurrent.SignallingRef

object Main extends App {
  def showTree(indent: Int, nodes: List[Planner.Node]): String = "" /*{
    val pad = "  " * indent
    nodes
      .map { n =>
        val thisInfo =
          pad + s"name: ${n.name}, cost: ${n.cost.toInt}), start: ${n.start}, end: ${n.end}, id: ${n.id}\n"
        thisInfo + n.children.toNel.map(showTree(indent + 1, _)).mkString_("")
      }
      .mkString_("")
  }*/

  def showDiff_(fa: NonEmptyList[Planner.Node], fb: NonEmptyList[Planner.Node], maxEnd: Double): String = "" /*{
    fa.sortBy(_.id)
      .zip(fb.sortBy(_.id))
      .map { case (a, b) =>
        val per = math.max((maxEnd / 40d).toInt, 1)
        val thisInfo =
          if (a.end.toInt != b.end.toInt) {
            (" " * (b.start.toInt / per)) + AnsiColor.RED_B + s"name: ${b.name}, batchName: ${b.batchName}, cost: ${b.cost.toInt}, start: ${b.start}, end: ${b.end}, id: ${b.id}" + AnsiColor.RESET + "\n" +
              (" " * (b.start.toInt / per)) + AnsiColor.BLUE_B + (">" * ((a.start - b.start).toInt / per)) + AnsiColor.GREEN_B + s"name: ${a.name}, batchName: ${b.batchName}, cost: ${a.cost.toInt}, start: ${a.start}, end: ${a.end}, id: ${a.id}" + AnsiColor.RESET + "\n"
          } else
            (" " * (a.start.toInt / per)) + s"name: ${a.name}, batchName: ${b.batchName}, cost: ${a.cost.toInt}, start: ${a.start}, end: ${a.end}, id: ${a.id}\n"

        thisInfo + a.children.toNel.map(showDiff_(_, b.children.toNel.get, maxEnd)).mkString_("")
      }
      .mkString_("")
  }*/

  def showDiff(fa: NonEmptyList[Planner.Node], fb: NonEmptyList[Planner.Node]) = "" /*{
    val me = Planner.NodeTree(fa).flattened.maximumBy(_.end).end
    AnsiColor.RED_B + "old field schedule" + AnsiColor.RESET + "\n" +
      AnsiColor.GREEN_B + "new field schedule" + AnsiColor.RESET + "\n" +
      AnsiColor.BLUE_B + "new field offset (deferral of execution)" + AnsiColor.RESET + "\n" +
      showDiff_(fa, fb, me)
  }*/

  def planCost(nodes: NonEmptyList[Planner.Node]): Double = 0d /*{
    val fnt = Planner.NodeTree(nodes).flattened

    fnt
      .groupBy(_.name)
      .toList
      .collect { case (_, nodes) =>
        val c = nodes.head.cost
        val e = nodes.head.elemCost
        val costCnt = nodes.groupBy(_.start.toInt)
        val groups = costCnt.size
        val batched = nodes.size - groups
        groups * c + batched * e
      }
      .sumAll
  }*/

  val q = """
query FragmentTyping {
  profiles(handles: ["zuck", "cocacola"]) {
    handle
    ...userFragment
    ...pageFragment
  }
}

fragment userFragment on User {
  friends {
    count
  }
}

fragment pageFragment on Page {
  likers {
    count
  }
}
  """

  val q2 = """
query withNestedFragments {
  user(id: 4) {
    friends(first: 10) {
      ...friendFields
    }
    mutualFriends(first: 10) {
      ...friendFields
    }
  }
}

fragment friendFields on User {
  id
  name
  ...standardProfilePic
}

fragment standardProfilePic on User {
  profilePic(size: 50)
}
  """

  val q3 = """
query inlineFragmentTyping {
  profiles(handles: ["zuck", "cocacola"]) {
    handle
    ... on User {
      friends {
        count
      }
    }
    ... on Page {
      likers {
        count
      }
    }
  }
}
  """

  val q4 = """
query inlineFragmentNoType($expandedInfo: Boolean) {
  user(handle: "zuck") {
    id
    name
    ... @include(if: $expandedInfo) {
      firstName
      lastName
      birthday
    }
  }
}
  """

  val tq = "\"\"\""
  val q5 = s"""
mutation {
  sendEmail(message: $tq
    Hello,
      World!

    Yours,
      GraphQL.
  $tq)
}
"""

  val q6 = s"""
query {
  user(id: 4) {
    id
    name
    smallPic: profilePic(size: 64)
    bigPic: profilePic(size: 1024)
  }
}
"""

  val q0 = """
query withNestedFragments {
  getData {
    ... on Data {
      a
      b
      c {
        ... DataFragment
      }
    }
  }
}

    fragment DataFragment on Data {
      a
      b
      c {
        ... NestedData
      }
    }

    fragment NestedData on Data {
      a
      b
      c {
        ... NestedData2
      }
    }

    fragment NestedData2 on Data {
      a
      b
    }
  """

  // val p = QueryParser.executableDefinition.rep
  // def tryParse[A](p: cats.parse.Parser[A], q: String): Unit =
  //   p.parseAll(q) match {
  //     case Left(e) =>
  //       val (left, right) = q.splitAt(e.failedAtOffset)
  //       val conflict = s"<<${q(e.failedAtOffset)}>>"
  //       val chunk = s"${left.takeRight(40)}$conflict${right.drop(1).take(40)}"
  //       val c = q(e.failedAtOffset)
  //       println(s"failed with char $c at offset ${e.failedAtOffset} with code ${c.toInt}: ${e.expected}")
  //       println(chunk)
  //     case Right(x) => println(x)
  //   }

  // final case class Data[F[_]](
  //     a: String,
  //     b: F[Int],
  //     c: F[Seq[Data[F]]]
  // )

  // final case class OtherData[F[_]](
  //     value: String,
  //     d1: F[Data[F]]
  // )

  // sealed trait Datas[F[_]]
  // object Datas {
  //   final case class Other[F[_]](value: OtherData[F]) extends Datas[F]
  //   final case class Dat[F[_]](value: Data[F]) extends Datas[F]
  // }

  // def getFriends[F[_]](name: String)(implicit F: Sync[F]): F[Seq[Data[F]]] =
  //   if (name == "John") F.delay(getData[F]("Jane")).map(List(_))
  //   else if (name == "Jane") F.delay(getData[F]("John")).map(List(_))
  //   else F.pure(Nil)

  // def getData[F[_]](name: String)(implicit F: Sync[F]): Data[F] =
  //   Data[F](
  //     name,
  //     F.delay(if (name == "John") 22 else 20),
  //     F.defer(getFriends[F](name))
  //   )
  // IO.monotonic.map(_.toMillis)

  // import gql.syntax.out._

  // final case class Deps(v: String)

  // def testSchemaShape[F[_]](implicit F: Async[F], Ask: Ask[F, Deps]): State[SchemaState[F], SchemaShape[F, Unit, Unit, Unit]] = {
  //   final case class IdentityData(value: Int, value2: String)

  //   final case class InputData(
  //       value: Int,
  //       val2: String,
  //       x3: Option[Seq[String]] = None
  //   )

  //   final case class ServerData(value: Int)

  //   final case class DataIds(ids: List[Int])

  //   BatchResolver[F, Int, ServerData](xs => F.pure(xs.map(x => x -> ServerData(x)).toMap)).map { serverDataBatcher =>
  //     implicit val inputDataType: In[InputData] = InputSyntax.obj[InputData](
  //       "InputData",
  //       (
  //         arg[Int]("value", 42),
  //         arg[String]("val2"),
  //         dsl.arg[Option[Seq[String]]]("x3", dsl.default.none)
  //       ).mapN(InputData.apply)
  //     )

  //     val valueArgs: Arg[(Int, String, Seq[String])] =
  //       (
  //         (
  //           arg[Int]("num", 42),
  //           arg[Int]("num2", 9),
  //           arg[Int]("num3", 99)
  //         ).mapN(_ + _ + _),
  //         arg[String]("text"),
  //         arg[Seq[String]]("xs", Seq.empty)
  //       ).tupled

  //     val inputDataArg = arg[InputData]("input")

  //     implicit def identityDataType: Type[F, IdentityData] =
  //       obj[F, IdentityData](
  //         "IdentityData",
  //         "value" -> effect((valueArgs, inputDataArg).tupled) { case (x, ((y, z, hs), i)) =>
  //           F.pure(s"${x.value2} + $z - ${(x.value + y).toString()} - (${hs.mkString(",")}) - $i")
  //         }
  //       )

  //     implicit def serverData: Type[F, ServerData] =
  //       obj[F, ServerData](
  //         "ServerData",
  //         "value" -> pure(_.value)
  //       )

  //     import gql.dsl.{stream, streamFallible, field}
  //     implicit lazy val dataType: Type[F, Data[F]] =
  //       obj2[F, Data[F]]("Data") { f =>
  //         fields(
  //           "dep" -> f(eff(_ => Ask.reader(_.v))),
  //           "a" -> f(full(x => IorT.bothT[F]("Oh no, an error!", x.a))),
  //           "a2" -> f(arg[Int]("num"))(pur { case (i, _) => i.a }),
  //           "b" -> f(eff(_.b)),
  //           "sd" -> f(
  //             serverDataBatcher
  //               .map { case (_, m) => m.values.toSeq }
  //               .contramapF(x => x.b.map(i => Set(i, i + 1, i * 2).rightIor))
  //           ),
  //           "c" -> f(eff(_.c.map(_.toSeq))),
  //           "doo" -> f(pur(_ => Vector(Vector(Vector.empty[String])))),
  //           "nestedSignal" -> field(stream { k =>
  //             fs2.Stream.eval(k.c.map(_.head)).repeat.metered((if (k.a == "John") 10 else 50).millis)
  //           }),
  //           "nestedSignal2" -> field(stream { k =>
  //             fs2.Stream
  //               .eval(k.c.map(_.head))
  //               .map(_.copy(a = if (scala.util.Random.nextDouble() > 0.5) "Jane" else "John"))
  //               .repeat
  //               .metered((if (k.a == "John") 10 else 50).millis)
  //           }),
  //           "testdefault" -> dsl.pure(
  //             dsl.arg[InputData](
  //               "input",
  //               dsl.default.obj(
  //                 "value" -> dsl.default(422),
  //                 "val2" -> dsl.default("test"),
  //                 "x3" -> dsl.default.arr(
  //                   Seq(
  //                     dsl.default("test")
  //                     // dsl.default.obj(
  //                     //   "v3" -> dsl.default("@@")
  //                     // )
  //                   )
  //                 )
  //                 // "ar2" -> dsl.default.arr(
  //                 //   Seq(
  //                 //     dsl.default.obj(
  //                 //       "v23" -> dsl.default("@@")
  //                 //     )
  //                 //   )
  //                 // ),
  //                 // "ar3" -> dsl.default.arr(
  //                 //   Seq(
  //                 //     dsl.default.obj(
  //                 //       "v33" -> dsl.default("@@")
  //                 //     )
  //                 //   )
  //                 // ),
  //                 // "ar4" -> dsl.default.arr(
  //                 //   Seq(
  //                 //     dsl.default.obj(
  //                 //       "v43" -> dsl.default("@@"),
  //                 //       "v44" -> dsl.default("@@"),
  //                 //       "v45" -> dsl.default("@@"),
  //                 //       "v46" -> dsl.default("@@"),
  //                 //       "v54" -> dsl.default("@@"),
  //                 //       "v55" -> dsl.default("@@"),
  //                 //       "v56" -> dsl.default("@@"),
  //                 //       "v57" -> dsl.default("@@")
  //                 //     ),
  //                 //     dsl.default.obj(
  //                 //       "v43" -> dsl.default("@@"),
  //                 //       "v44" -> dsl.default("@@"),
  //                 //       "v45" -> dsl.default("@@"),
  //                 //       "v46" -> dsl.default("@@"),
  //                 //       "v54" -> dsl.default("@@"),
  //                 //       "v55" -> dsl.default("@@"),
  //                 //       "v56" -> dsl.default("@@"),
  //                 //       "v57" -> dsl.default("@@")
  //                 //     )
  //                 //   )
  //                 // ),
  //                 // "ar5" -> dsl.default.arr(
  //                 //   Seq(
  //                 //     dsl.default.obj(
  //                 //       "v43" -> dsl.default("@@"),
  //                 //       "v44" -> dsl.default("@@"),
  //                 //       "v45" -> dsl.default("@@"),
  //                 //       "v46" -> dsl.default("@@"),
  //                 //       "v54" -> dsl.default("@@"),
  //                 //       "v55" -> dsl.default("@@"),
  //                 //       "v56" -> dsl.default("@@"),
  //                 //       "v57" -> dsl.default("@@")
  //                 //     )
  //                 //   )
  //                 // )
  //               )
  //             )
  //           ) { case (_, _) => "" }
  //           // "test" -> field(stream(k => fs2.Stream(0))),
  //           // "test" -> field(arg[Int]("num"))(stream { case (k, a) => fs2.Stream(0) }),
  //           // "test" -> field(streamFallible(k => fs2.Stream(NonEmptyChain("errrrr").leftIor[String]))),
  //           // "test" -> field(arg[Int]("num"))(streamFallible { case (k, a) =>
  //           //   fs2.Stream(NonEmptyChain("errrrr").leftIor[String])
  //           // }),
  //           // "test" -> {
  //           //   val a: Arg[(Int, Option[Int])] = (arg[Int]("numDice"), arg[Option[Int]]("numSides")).tupled
  //           //   val r: BatchResolver[F, (Data[F], Int), String] = ???
  //           //   field(stream(r)(k => fs2.Stream(0)))
  //           // }
  //         )
  //       }

  //     implicit def otherDataType: Type[F, OtherData[F]] =
  //       obj[F, OtherData[F]](
  //         "OtherData",
  //         "value" -> pure(_.value),
  //         "d1" -> effect(_.d1)
  //       )

  //     implicit def datasType: Union[F, Datas[F]] =
  //       union[F, Datas[F]](
  //         "Datas",
  //         contra[Data[F]] { case Datas.Dat(d) => d },
  //         contra[OtherData[F]] { case Datas.Other(o) => o }
  //       )

  //     trait A {
  //       def a: String
  //     }
  //     object A {
  //       implicit def t: Interface[F, A] =
  //         interface[F, A](
  //           obj(
  //             "A",
  //             "a" -> pure(_ => "A")
  //           ),
  //           contra[B] { case b: B => b },
  //           contra[C] { case c: C => c }
  //         )
  //     }

  //     trait D {
  //       def d: String
  //     }
  //     object D {
  //       implicit def t: Interface[F, D] =
  //         interface[F, D](
  //           obj(
  //             "D",
  //             "d" -> pure(_ => "D")
  //           ),
  //           contra[C] { case c: C => c }
  //         )
  //     }

  //     final case class B(a: String) extends A
  //     object B {
  //       implicit def t: Type[F, B] = obj[F, B]("B", "a" -> pure(_ => "B"), "b" -> pure(_ => Option("BO")))
  //     }
  //     final case class C(a: String, d: String) extends A with D
  //     object C {
  //       implicit def t: Type[F, C] =
  //         obj[F, C]("C", "a" -> pure(_ => "C"), "d" -> pure(_ => "D"), "fail" -> full2(_ => IorT.leftT[F, String]("im dead")))
  //     }

  //     SchemaShape[F, Unit, Unit, Unit](
  //       obj[F, Unit](
  //         "Query",
  //         "getData" -> pure(_ => root[F]),
  //         "getDatas" -> pure(_ => datasRoot[F]),
  //         "getInterface" -> pure(_ => (C("hey", "tun"): A)),
  //         "getOther" -> pure(_ => (C("hey", "tun"): D)),
  //         "doIdentity" -> pure(_ => IdentityData(2, "hello"))
  //       ).some
  //     )
  //   }
  // }

  // def root[F[_]: Sync]: Data[F] = getData[F]("John")

  // def datasRoot[F[_]: Async]: Datas[F] =
  //   Datas.Other(
  //     OtherData(
  //       "toplevel",
  //       Async[F].delay(getData[F]("Jane"))
  //     )
  //   )

  // val qn = """
// query withNestedFragments {
  //  getDatas {
  //    ... Frag
  //  }
  //  getData {
  //    ... F2
  //  }
  // getInterface {
  //   ... F3
  // }
// }

// fragment F4 on A {
  // a
// }

// fragment F3 on A {
  // ... on B {
  //   a
  // }
  // ... on C {
  //   a
  //   fail
  // }
// }

// fragment F2 on Data {
  // a
  // b
  // sd {
  //   value
  // }
  // c {
  //   a
  //   b
  // }
// }

  // fragment Frag on Datas {
  //   ... on OtherData {
  //     value
  //     d1 {
  //       a
  //       b
  //       sd {
  //         value
  //       }
  //       c {
  //         ... F2
  //       }
  //     }
  //   }
  // }
  // """

  // type D[A] = Kleisli[IO, Deps, A]

  // def mainProgram[F[_]](implicit F: Async[F], A: Ask[F, Deps], C: std.Console[F]): F[Unit] = {
  //   Schema.stateful(testSchemaShape[F]).flatMap { schema =>
  //     println(schema.shape.render)
  //     // System.exit(0)

  //     println(schema.shape.validate.map(_.toString).mkString_("\n"))

  //     def parseAndPrep(q: String): Option[NonEmptyList[PreparedQuery.PreparedField[F, Any]]] =
  //       gql.parser.parse(q).map(PreparedQuery.prepare(_, schema, Map.empty)) match {
  //         case Left(e) =>
  //           println(e.prettyError.value)
  //           None
  //         case Right(Left(x)) =>
  //           println(x)
  //           None
  //         case Right(Right(x)) => Some(x)
  //       }

  //     val qsig = """
// query withNestedFragments {
  // getData {
  //   dep
  //   d2lol: dep
  //   doo
  //   nestedSignal2 {
  //     a
  //   }
  //   nestedSignal {
  //     a
  //     nestedSignal {
  //       nestedSignal2 {
  //         a
  //       }
  //       a
  //       nestedSignal {
  //         a
  //         nestedSignal {
  //           nestedSignal2 {
  //             a
  //           }
  //           a
  //         }
  //       }
  //     }
  //   }
  // }
// }
  // """

  //     implicit def p[F[_]: Applicative] = Planner[F]
  //     F.fromOption(parseAndPrep(qn), new Exception(":((")).flatMap { x =>
  //       Statistics[F].flatMap { implicit stats =>
  //         Planner.costTree[F](x).flatMap { costTree =>
  //           println(showTree(0, costTree.root))

  //           interpreter.Interpreter
  //             .runSync[F]((), x, schema.state)
  //             .flatMap { case (failures, x) =>
  //               C.println(failures.flatMap(_.asGraphQL)) >> C.println(x)
  //             }
  //         }
  //       }
  //     } >>
  //       F.fromOption(parseAndPrep(qsig), new Exception(":((")).flatMap { x =>
  //         Statistics[F].flatMap { implicit stats =>
  //           Planner.costTree[F](x).flatMap { costTree =>
  //             println(showTree(0, costTree.root))

  //             interpreter.Interpreter
  //               .runStreamed[F]((), x, schema.state)
  //               .zipWithIndex
  //               .evalMap { case ((failures, x), i) =>
  //                 C.println(s"got new subtree $i") >>
  //                   C.println("errors:") >>
  //                   C.println(failures.flatMap(_.asGraphQL)) >>
  //                   C.println(x.toString())
  //               }
  //               .take(0)
  //               .compile
  //               .drain
  //           }
  //         }
  //       }
  //   }
  // }

  // mainProgram[D].run(Deps("hey")).unsafeRunSync()

  // gql.parser
  //   .parse(
  //     """
  // query {
  //   field1
  //   field2(test: 42)
  // }
  
  // fragment test on Test {
  //   -value1
  //   value2 
  // }
// """
  //   )
  //   .leftMap(x => println(x.prettyError.value))

  // Test.go
  // // SangriaTest.run
// }

// object Test {
  // import cats._

  // import gql._
  // import gql.dsl._

  // final case class Data(str: String)

  // final case class InputStuff(
  //     a: String,
  //     b: Int
  // )

  // implicit val inputForInputStuff = input(
  //   "InputStuff",
  //   (
  //     arg[String]("a"),
  //     arg[Int]("b", 42)
  //   ).mapN(InputStuff.apply)
  // )

  // tpe[Id, Data](
  //   "Something",
  //   "field" -> pure(arg[String]("arg1", default("default"))) { case (_, _) => "" },
  //   "field2" -> pure(
  //     arg[InputStuff](
  //       "arg1",
  //       default.obj(
  //         "a" -> default("a"),
  //         "b" -> default(43),
  //         "c" -> default.arr(
  //           Seq(
  //             default("42"),
  //             default(43)
  //           )
  //         )
  //       )
  //     )
  //   ) { case (_, _) => "" }
  // )

  // def go = {
  //   import cats.effect.implicits._

  //   final case class VpnData(
  //       content: String,
  //       hash: String,
  //       connectedUser: String,
  //       serverId: String
  //   )

  //   type Username = String

  //   trait VpnConnection[F[_]] {
  //     def getName: F[String]

  //     def getCreatedAge: F[Int]

  //     def getDataUpdates: fs2.Stream[F, VpnData]
  //   }

  //   object VpnConnection {
  //     // Connection aquisition is very slow
  //     def apply[F[_]](userId: Username, serverId: String)(implicit F: Async[F]): Resource[F, VpnConnection[F]] = {
  //       import scala.concurrent.duration._

  //       Resource.eval(F.monotonic).flatMap { before =>
  //         Resource.make[F, VpnConnection[F]] {
  //           F.delay(println("Connecting to VPN...")).delayBy(500.millis).as {
  //             new VpnConnection[F] {
  //               def getName = F.delay("super_secret_file")

  //               def getCreatedAge = F.delay(42)

  //               def getDataUpdates =
  //                 fs2
  //                   .Stream(1)
  //                   .repeat
  //                   .scan(0)(_ + _)
  //                   .lift[F]
  //                   .metered(50.millis)
  //                   .map { x => println("emitting"); x }
  //                   .map(i => VpnData(s"content $i", s"hash of $i", userId, serverId))
  //             }
  //           }
  //         }(_ =>
  //           F.monotonic.map { after =>
  //             println(s"Disconnecting from VPN after ${(after - before).toMillis}ms ...")
  //           }
  //         )
  //       }
  //     }
  //   }

  //   final case class WithVpn[F[_], A](
  //       vpn: VpnConnection[F],
  //       value: A
  //   )

  //   final case class VpnMetadata(subscriptionTimestamp: String)

  //   def currentTimestamp[F[_]](implicit F: Applicative[F]): F[String] = F.pure("now!")

  //   implicit def vpnMetadata[F[_]: Applicative] = tpe[F, WithVpn[F, VpnMetadata]](
  //     "VpnMetadata",
  //     "name" -> eff(_.vpn.getName),
  //     "createdAge" -> eff(_.vpn.getCreatedAge),
  //     "subscriptionTimestamp" -> pure(_.value.subscriptionTimestamp)
  //   )

  //   implicit def vpnData[F[_]: Applicative] = tpe[F, VpnData](
  //     "VpnData",
  //     "content" -> pure(_.content),
  //     "hash" -> pure(_.hash),
  //     "connectedUser" -> pure(_.connectedUser),
  //     "serverId" -> pure(_.serverId)
  //   )

  //   implicit def vpn[F[_]: Applicative] = tpe[F, VpnConnection[F]](
  //     "Vpn",
  //     "metadata" -> eff(conn => currentTimestamp[F].map(ts => WithVpn(conn, VpnMetadata(ts)))),
  //     "data" -> field(stream(_.getDataUpdates))
  //   )

  //   def root[F[_]: Async] =
  //     tpe[F, Username](
  //       "Subscription",
  //       "vpn" -> field(arg[String]("serverId"))(stream[F, (Username, String), VpnConnection[F]] { case (userId, serverId) =>
  //         fs2.Stream.resource(VpnConnection[F](userId, serverId))
  //       }.andThen(EffectResolver[F, VpnConnection[F], VpnConnection[F]](x => Applicative[F].pure(Ior.left("wak wak waa"))))),
  //       "me" -> pure(identity)
  //     )

  //   def subscriptionQuery = """
// subscription($serverId: String!) {
  // vpn(serverId: $serverId) {
  //   metadata {
  //     name
  //     createdAge
  //     subscriptionTimestamp
  //   }
  //   data {
  //     content
  //     hash
  //     connectedUser
  //     serverId
  //   }
  // }
// }
// """

  //   def runVPNSubscription(q: String, n: Int, subscription: Type[IO, Username] = root[IO]) =
  //     Schema.simple(SchemaShape[IO, Unit, Unit, Username](subscription = subscription.some)).flatMap { sch =>
  //       Compiler[IO]
  //         .compile(sch, q, variables = Map("serverId" -> Json.fromString("abc123")), subscriptionInput = IO.pure("john_doe"))
  //         .traverse { case Application.Subscription(stream) =>
  //           stream.take(n).map(_.asGraphQL).compile.toList
  //         }
  //     }

  //   // runVPNSubscription(subscriptionQuery, 3).unsafeRunSync()

  //   def oauthAccessToken[F[_]: Async](username: Username): fs2.Stream[F, Username] =
  //     fs2
  //       .Stream(username)
  //       .lift[F]
  //       .repeat
  //       .metered(110.millis)
  //       .zipWithIndex
  //       .map(i => s"token $i")

  //   def root2[F[_]: Async] =
  //     tpe[F, Username](
  //       "Subscription",
  //       "vpn" -> field(arg[String]("serverId"))(stream[F, (Username, String), VpnConnection[F]] { case (userId, serverId) =>
  //         oauthAccessToken[F](userId).map { un => println(un); un }.flatMap { token =>
  //           fs2.Stream.resource(VpnConnection[F](token, serverId)).map { con => println(con); con }
  //         }
  //       }.andThen(EffectResolver[F, VpnConnection[F], VpnConnection[F]](x => Applicative[F].pure(Ior.left("wak wak waa")))))
  //     )

  //   println(runVPNSubscription(subscriptionQuery, 1, root2[IO]).unsafeRunSync() match {
  //     case Left(CompilationError.Parse(pe))       => pe.prettyError.value
  //     case Left(CompilationError.Preparation(pe)) => pe.message
  //     case Right(x)                               => x.toString()
  //   })
  // }
}