package gql

import cats.implicits._
import cats.effect._
import cats.data._
import gql.dsl._
import gql.ast._
import munit.CatsEffectSuite
import io.circe._

// https://github.com/graphql/graphql-js/blob/main/src/__tests__/starWarsData.ts
object StarWarsSchema {
  val luke = Human(
    "1000",
    "Luke Skywalker".some,
    "1002" :: "1003" :: "2000" :: "2001" :: Nil,
    Episode.NEWHOPE :: Episode.EMPIRE :: Episode.JEDI :: Nil,
    "Tatooine".some
  )

  val vader = Human(
    "1001",
    "Darth Vader".some,
    "1004" :: Nil,
    Episode.NEWHOPE :: Episode.EMPIRE :: Episode.JEDI :: Nil,
    "Tatooine".some
  )

  val han = Human(
    "1002",
    "Han Solo".some,
    "1000" :: "1003" :: "2001" :: Nil,
    Episode.NEWHOPE :: Episode.EMPIRE :: Episode.JEDI :: Nil,
    None
  )

  val leia = Human(
    "1003",
    "Leia Organa".some,
    "1000" :: "1002" :: "2000" :: "2001" :: Nil,
    Episode.NEWHOPE :: Episode.EMPIRE :: Episode.JEDI :: Nil,
    "Alderaan".some
  )

  val tarkin = Human(
    "1004",
    "Wilhuff Tarkin".some,
    "1001" :: Nil,
    Episode.NEWHOPE :: Nil,
    None
  )

  val humanData =
    List(luke, vader, han, leia, tarkin)
      .map(x => x.id -> x)
      .toMap

  val threepio = Droid(
    "2000",
    "C-3PO".some,
    "1000" :: "1002" :: "1003" :: "2001" :: Nil,
    Episode.NEWHOPE :: Episode.EMPIRE :: Episode.JEDI :: Nil,
    "Protocol"
  )

  val artoo = Droid(
    "2001",
    "R2-D2".some,
    "1000" :: "1002" :: "1003" :: Nil,
    Episode.NEWHOPE :: Episode.EMPIRE :: Episode.JEDI :: Nil,
    "Astromech"
  )

  val droidData =
    List(threepio, artoo)
      .map(x => x.id -> x)
      .toMap

  def getCharacter(id: String): IO[Option[Character]] =
    IO(humanData.get(id) orElse droidData.get(id))

  def getFriends(character: Character): IO[List[Character]] =
    character.friends.flatTraverse(getCharacter(_).map(_.toList))

  def getHero(episode: Option[Episode]): IO[Character] =
    if (episode.contains(Episode.EMPIRE)) IO(luke)
    else IO(artoo)

  def getHuman(id: String): IO[Option[Human]] =
    IO(humanData.get(id))

  def getDroid(id: String): IO[Option[Droid]] =
    IO(droidData.get(id))

  lazy val schemaShape = {
    implicit lazy val episode: Enum[IO, Episode] =
      enum[IO, Episode](
        "Episode",
        enumInst("NEWHOPE", Episode.NEWHOPE),
        enumInst("EMPIRE", Episode.EMPIRE),
        enumInst("JEDI", Episode.JEDI)
      )

    implicit lazy val character: Interface[IO, Character] =
      interface[IO, Character](
        "Character",
        "id" -> pure(_.id),
        "name" -> pure(_.name),
        "friends" -> eff(getFriends),
        "appearsIn" -> pure(_.appearsIn),
        "secretBackstory" -> fallible(_ => IO("secretBackstory is secret.".leftIor[String]))
      )(
        instance[Human] { case h: Human => h },
        instance[Droid] { case d: Droid => d }
      )

    implicit lazy val human: Type[IO, Human] =
      tpe[IO, Human](
        "Human",
        "homePlanet" -> pure(_.homePlanet),
        character.fields.toList: _*
      )

    implicit lazy val droid: Type[IO, Droid] =
      tpe[IO, Droid](
        "Droid",
        "primaryFunction" -> pure(_.primaryFunction),
        character.fields.toList: _*
      )

    SchemaShape[IO, Unit, Unit, Unit](
      Some(
        tpe[IO, Unit](
          "Query",
          "hero" -> eff(arg[Option[Episode]]("episode")) { case (_, ep) => getHero(ep) },
          "human" -> eff(arg[String]("id")) { case (_, id) => getHuman(id) },
          "droid" -> eff(arg[String]("id")) { case (_, id) => getDroid(id) }
        )
      )
    )
  }

  lazy val schema = Schema.simple(schemaShape)

  sealed trait Episode

  object Episode {
    case object NEWHOPE extends Episode
    case object EMPIRE extends Episode
    case object JEDI extends Episode
  }

  trait Character {
    def id: String
    def name: Option[String]
    def friends: List[String]
    def appearsIn: List[Episode]
  }

  final case class Human(
      id: String,
      name: Option[String],
      friends: List[String],
      appearsIn: List[Episode],
      homePlanet: Option[String]
  ) extends Character

  final case class Droid(
      id: String,
      name: Option[String],
      friends: List[String],
      appearsIn: List[Episode],
      primaryFunction: String
  ) extends Character
}