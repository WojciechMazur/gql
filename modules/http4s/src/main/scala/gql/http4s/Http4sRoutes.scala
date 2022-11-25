/*
 * Copyright 2022 Valdemar Grange
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
package gql.http4s

import cats.effect._
import cats.implicits._
import io.circe._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import gql._
import org.http4s.server.websocket.WebSocketBuilder
import gql.graphqlws.GraphqlWS
import org.http4s.websocket.WebSocketFrame
import org.typelevel.ci._

final case class Http4sCompilerParametes(
    compilerParameters: CompilerParameters,
    headers: Headers
)

object Http4sRoutes {
  protected implicit lazy val cd: Decoder[CompilerParameters] =
    io.circe.generic.semiauto.deriveDecoder[CompilerParameters]
  protected implicit def ed[F[_]: Concurrent]: EntityDecoder[F, CompilerParameters] =
    org.http4s.circe.jsonOf[F, CompilerParameters]

  type Compiler[F[_]] = Http4sCompilerParametes => F[Either[Response[F], Compiler.Outcome[F]]]

  def sync[F[_]](
      compiler: Compiler[F],
      path: String = "graphql"
  )(implicit F: Concurrent[F]): HttpRoutes[F] = {
    val d = new Http4sDsl[F] {}
    import d._
    import io.circe.syntax._
    import org.http4s.circe._

    HttpRoutes.of[F] { case r @ POST -> Root / `path` =>
      r.as[CompilerParameters].flatMap { params =>
        compiler(Http4sCompilerParametes(params, r.headers)).flatMap {
          case Left(response)                                => F.pure(response)
          case Right(Left(pe: CompilationError.Parse))       => Ok(pe.asGraphQL.asJson)
          case Right(Left(pe: CompilationError.Preparation)) => Ok(pe.asGraphQL.asJson)
          case Right(Right(app)) =>
            val fa = app match {
              case Application.Mutation(run)     => run
              case Application.Query(run)        => run
              case Application.Subscription(run) => run.take(1).compile.lastOrError
            }

            fa.flatMap(qr => Ok(qr.asGraphQL.asJson))
        }
      }
    }
  }

  def ws[F[_]](
      getCompiler: GraphqlWS.GetCompiler[F],
      wsb: WebSocketBuilder[F],
      path: String = "ws"
  )(implicit F: Async[F]): HttpRoutes[F] = {
    val d = new Http4sDsl[F] {}
    import d._
    import io.circe.syntax._

    HttpRoutes.of[F] { case GET -> Root / `path` =>
      GraphqlWS[F](getCompiler).allocated.flatMap { case ((toClient, fromClient), close) =>
        wsb
          .withOnClose(close)
          .withFilterPingPongs(true)
          .withHeaders(Headers(Header.Raw(ci"Sec-WebSocket-Protocol", "graphql-transport-ws")))
          .build(
            toClient.evalMap[F, WebSocketFrame] {
              case Left(te) => F.fromEither(WebSocketFrame.Close(te.code, te.message))
              case Right(x) => F.pure(WebSocketFrame.Text(x.asJson.noSpaces))
            },
            _.evalMap[F, Option[String]] {
              case WebSocketFrame.Text(x, true) => F.pure(Some(x))
              case _: WebSocketFrame.Close      => F.pure(None)
              // case c: WebSocketFrame.Close if c.closeCode == 1000 => F.pure(None)
              // case other                                          => F.raiseError(new Exception(s"Unexpected frame: $other"))
            }.unNone
              .evalMap { x => F.fromEither(io.circe.parser.decode[GraphqlWS.FromClient](x)) }
              .through(fromClient)
          )
      }
    }
  }
}
