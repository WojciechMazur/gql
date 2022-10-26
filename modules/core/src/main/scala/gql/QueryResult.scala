package gql

import io.circe._
import cats.data._
import cats.implicits._
import gql.interpreter._

final case class QueryResult(
    errors: Chain[EvalFailure],
    data: JsonObject
) {
  lazy val asGraphQL: JsonObject = {
    import io.circe.syntax._
    Map(
      "errors" -> errors.map(_.asGraphQL).toList.flatMap(_.toList).toNel.map(_.asJson),
      "data" -> Some(data).filter(_.nonEmpty).map(_.asJson)
    ).collect { case (k, Some(v)) => k -> v }.asJsonObject
  }
}