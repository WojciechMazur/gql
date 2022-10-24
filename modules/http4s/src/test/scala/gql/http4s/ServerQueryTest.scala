package gql.http4s

import org.http4s.implicits._
import io.circe._
import munit.CatsEffectSuite
import gql._
import cats.effect._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import org.http4s.circe._

class ServerQueryTest extends CatsEffectSuite {
  lazy val swSchema = StarWarsSchema.schema.unsafeRunSync()

  lazy val swCompiler = Compiler[IO].make(swSchema)

  lazy val swHttp4sCompiler = Http4sCompiler.fromCompiler(swCompiler)

  lazy val http4sRoutes = gql.http4s.Http4sRoutes.sync[IO](swHttp4sCompiler)

  lazy val client = Client.fromHttpApp(http4sRoutes.orNotFound)

  def forceParse(s: String): Json = {
    val x = io.circe.parser.parse(s)
    assert(clue(x).isRight)
    x.toOption.get
  }

  test("should be able to fire some queries off against the http4s routes") {
    val body = Json.obj(
      "query" -> Json.fromString {
        """
          query NestedQuery {
            hero {
              name
              friends {
                name
                appearsIn
                friends {
                  name
                }
              }
            }
          }
      """
      }
    )

    client
      .expect[Json](POST(body, uri"https://api.acme.org/graphql"))
      .map { b =>
        assertEquals(
          b,
          forceParse {
            """
        {
          "data": {
            "hero": {
              "name": "R2-D2",
              "friends": [
                {
                  "name": "Luke Skywalker",
                  "appearsIn": [
                    "NEWHOPE",
                    "EMPIRE",
                    "JEDI"
                  ],
                  "friends": [
                    {
                      "name": "Han Solo"
                    },
                    {
                      "name": "Leia Organa"
                    },
                    {
                      "name": "C-3PO"
                    },
                    {
                      "name": "R2-D2"
                    }
                  ]
                },
                {
                  "name": "Han Solo",
                  "appearsIn": [
                    "NEWHOPE",
                    "EMPIRE",
                    "JEDI"
                  ],
                  "friends": [
                    {
                      "name": "Luke Skywalker"
                    },
                    {
                      "name": "Leia Organa"
                    },
                    {
                      "name": "R2-D2"
                    }
                  ]
                },
                {
                  "name": "Leia Organa",
                  "appearsIn": [
                    "NEWHOPE",
                    "EMPIRE",
                    "JEDI"
                  ],
                  "friends": [
                    {
                      "name": "Luke Skywalker"
                    },
                    {
                      "name": "Han Solo"
                    },
                    {
                      "name": "C-3PO"
                    },
                    {
                      "name": "R2-D2"
                    }
                  ]
                }
              ]
            }
          }
        }
        """
          }
        )
      }
  }
}
