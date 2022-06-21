package gql

import cats.effect.implicits._
import cats.implicits._
import cats.data._
import cats.effect._
import cats.mtl._
import cats.mtl.implicits._
import cats._
import io.circe._
import gql.GQLParser.Value.BooleanValue
import gql.GQLParser.Value.VariableValue
import gql.GQLParser.Value.FloatValue
import gql.GQLParser.Value.IntValue
import gql.GQLParser.Value.EnumValue
import gql.GQLParser.Value.StringValue
import gql.GQLParser.Value.ObjectValue
import gql.GQLParser.Value.NullValue
import gql.GQLParser.Value.ListValue
import gql.GQLParser.OperationDefinition.Detailed
import gql.GQLParser.OperationDefinition.Simple
import gql.Types._

object PreparedQuery {
  /*
   * the query subset of the schema, with values closed in
   * lets us easily reason with query complexity, planning, re-execution and not bother with bad argument states
   *
   * `
   *   # "SubSelection" with type Fragment with fields that originate from D
   *   fragment SubSelection on D {
   *     dd # "dd" with type DataField with inner type Scalar
   *   }
   *
   *   fragment DataSelection on Data { # "DataSelection" with type Fragment with fields that originate from Data
   *     c { # "c" with type DataField and inner type Selection
   *       ... on C1 { "C1" with type InlineSpread which points to C1
   *         __typename # "__typename" with type DataField with inner type Scalar
   *       }
   *       ... on C2 { "C2" with type InlineSpread which points to C2
   *         __typename # "__typename" with type DataField with inner type Scalar
   *       }
   *     }
   *     d { # "d" with type DataField with inner type Selection
   *       ... SubSelection # "SubSelection" with type FragmentSpread which points to SubSelection
   *     }
   *   }
   *
   *   query {
   *     data { # "data" with type DataField and inner type Selection
   *       a # "a" with type DataField with inner type Scalar
   *       b { # "b" with type DataField with inner type Selection
   *         ba # "ba" with type DataField with inner type Scalar
   *       }
   *       ... DataSelection # "DataSelection" with type FragmentSpread which points to DataSelection
   *     }
   *   }
   * `
   */
  sealed trait Prepared[F[_], A]

  sealed trait PreparedField[F[_], A]

  final case class PreparedDataField[F[_], I, T](
      name: String,
      resolve: I => Types.Output.Fields.Resolution[F, T],
      selection: Prepared[F, T]
  ) extends PreparedField[F, I]

  final case class FragmentDefinition[F[_], A](
      name: String,
      typeCondition: String,
      runtimeCheck: Any => Boolean,
      fields: NonEmptyList[PreparedField[F, A]]
  )

  final case class PreparedFragmentReference[F[_], A](
      reference: FragmentDefinition[F, A]
  ) extends PreparedField[F, A]

  final case class PreparedInlineFragment[F[_], A](
      runtimeCheck: Any => Boolean,
      selection: Selection[F, A]
  ) extends PreparedField[F, A]

  final case class Selection[F[_], A](fields: NonEmptyList[PreparedField[F, A]]) extends Prepared[F, A]

  final case class PreparedList[F[_], A](of: Prepared[F, A]) extends Prepared[F, List[A]]

  final case class PreparedLeaf[F[_], A](name: String, encode: A => Either[String, Json]) extends Prepared[F, A]

  sealed trait FragmentAnalysis[F[_]]
  object FragmentAnalysis {
    final case class Cached[F[_]](fd: FragmentDefinition[F, Any]) extends FragmentAnalysis[F]
    final case class Unevaluated[F[_]](fd: GQLParser.FragmentDefinition) extends FragmentAnalysis[F]
  }

  final case class AnalysisState[F[_]](
      fragments: Map[String, FragmentAnalysis[F]],
      cycleSet: Set[String]
  )

  def valueName(value: GQLParser.Value): String = value match {
    case ObjectValue(_)   => "object"
    case EnumValue(_)     => "enum"
    case StringValue(_)   => "string"
    case IntValue(_)      => "int"
    case BooleanValue(_)  => "boolean"
    case VariableValue(_) => "variable"
    case ListValue(_)     => "list"
    case FloatValue(_)    => "float"
    case NullValue        => "null"
  }

  def parserValueToValue(value: GQLParser.Value, variableMap: Map[String, Json]): Either[String, Value] = {
    def go[F[_]](x: GQLParser.Value)(implicit
        F: MonadError[F, String],
        D: Defer[F]
    ): F[Value] =
      x match {
        case IntValue(v) => F.pure(Value.IntValue(v))
        case ObjectValue(v) =>
          D.defer {
            v.traverse { case (k, v) =>
              go[F](v).map(k -> _)
            }.map(xs => Value.ObjectValue(xs.toMap))
          }
        case ListValue(v) =>
          D.defer(v.toVector.traverse(go[F]).map(Value.ArrayValue(_)))
        case EnumValue(v) => F.pure(Value.EnumValue(v))
        case VariableValue(v) =>
          F.fromOption(variableMap.get(v).map(Value.JsonValue(_)), s"variable $v not found")
        case NullValue       => F.pure(Value.NullValue)
        case BooleanValue(v) => F.pure(Value.BooleanValue(v))
        case FloatValue(v)   => F.fromOption(v.toBigDecimal.map(Value.FloatValue(_)), s"$v is not a vaild json number")
        case StringValue(v)  => F.pure(Value.StringValue(v))
      }

    go[EitherT[Eval, String, *]](value).value.value
  }

  /*def getTypePossibleTypes[F[_], G[_]](t: Types.ObjectLike[G, _])(implicit
      F: MonadError[F, String],
      D: Defer[F]
  ): F[Set[String]] =
    D.defer {
      t match {
        case Output.Object(name, _) => F.pure(Set(name))
        case Output.Interface(_, instances, _) =>
          instances.traverse[F, Set[String]](x => getTypePossibleTypes[F, G](x.ot)).map(_.toSet.flatten)
      }
    }*/

  // https://spec.graphql.org/June2018/#sec-Fragment-spread-is-possible
  def getPossibleTypes[F[_], G[_]](typename: String, schema: Types.Schema[G, _])(implicit
      F: MonadError[F, String],
      D: Defer[F]
  ): F[Set[String]] =
    D.defer[Set[String]] {
      schema.types.get(typename) match {
        case None                                    => F.raiseError(s"type $typename not found")
        case Some(Output.Object(name, _))            => F.pure(Set(name))
        case Some(Output.Interface(_, instances, _)) => F.pure(instances.map(_.ot.name).toSet)
        case Some(Types.Output.Union(_, fields))     => F.pure(fields.map(_.name).toList.toSet)
        case Some(t)                                 => F.raiseError(s"type $typename is not an object or union, but instead ${t.name}")
      }
    }

  def prepareSelections[F[_], G[_]](
      s: GQLParser.SelectionSet,
      typename: String,
      types: NonEmptyList[(String, Types.Output.Fields.Field[G, _, _])],
      schema: Types.Schema[G, _],
      variableMap: Map[String, Json]
  )(implicit S: Stateful[F, AnalysisState[G]], F: MonadError[F, String], D: Defer[F]): F[NonEmptyList[PreparedField[G, Any]]] = D.defer {
    val schemaMap = types.toNem

    s.selections
      .traverse[F, PreparedField[G, Any]] {
        case GQLParser.Selection.FieldSelection(field) =>
          schemaMap.lookup(field.name) match {
            case None    => F.raiseError(s"unknown field name ${field.name}")
            case Some(f) =>
              // unify parameterized and non-prameterized fields by closing in parameters
              val closedProgram: F[(Any => Types.Output.Fields.Resolution[G, Any], Types.Output[G, Any])] =
                (f, field.arguments) match {
                  case (Types.Output.Fields.SimpleField(_, _), Some(_)) =>
                    F.raiseError(s"field ${field.name} has arguments, but none were expected")
                  case (Types.Output.Fields.SimpleField(resolve, graphqlType), None) =>
                    F.pure((resolve.asInstanceOf[Any => Types.Output.Fields.Resolution[G, Any]], graphqlType.value))
                  case (Types.Output.Fields.ArgField(args, _, _), None) =>
                    F.raiseError(s"no arguments provided for ${field.name}, expected ${args.entries.size}")
                  case (Types.Output.Fields.ArgField(args, resolve, graphqlType), Some(provided)) =>
                    val providedMap = provided.nel.toList.map(x => x.name -> x.value).toMap
                    val argResolution =
                      args.entries
                        .traverse { arg =>
                          providedMap
                            .get(arg.name) match {
                            case None    => arg.default.toRight(s"missing argument ${arg.name}")
                            case Some(x) => parserValueToValue(x, variableMap).flatMap(j => arg.input.decode(j))
                          }
                        }
                        .map(_.toList)

                    F.fromEither(argResolution)
                      .map(args.decode)
                      .map { resolvedArg =>
                        ((x: Any) => resolve.asInstanceOf[Any => Types.Output.Fields.Resolution[G, Any]](x, resolvedArg), graphqlType.value)
                      }
                }

              // before we can finish this field, we need to prepare sub-selections
              closedProgram.flatMap { case (resolve, tpe) =>
                val prepF: F[Prepared[G, Any]] =
                  (tpe, field.selectionSet) match {
                    case (ol: Types.ObjectLike[G, _], Some(ss)) =>
                      prepareSelections[F, G](ss, ol.name, ol.fields, schema, variableMap).map(Selection(_))
                    case (ol: Types.ObjectLike[G, _], None) =>
                      F.raiseError(s"object type ${ol.name} had no selections")
                    case (Types.Output.Arr(ol: Types.ObjectLike[G, _]), Some(ss)) =>
                      prepareSelections[F, G](ss, ol.name, ol.fields, schema, variableMap).map(Selection(_))
                    case (Types.Output.Arr(ol: Types.ObjectLike[G, _]), None) =>
                      F.raiseError(s"object type ${ol.name} in list had no selections")
                    case (Types.Output.Opt(ol: Types.ObjectLike[G, _]), Some(ss)) =>
                      prepareSelections[F, G](ss, ol.name, ol.fields, schema, variableMap).map(Selection(_))
                    case (Types.Output.Opt(ol: Types.ObjectLike[G, _]), None) =>
                      F.raiseError(s"optional object type ${ol.name} had no selections")
                    case (c @ Types.Output.Enum(_), None) =>
                      F.pure(PreparedLeaf(c.codec.name, (x: Any) => c.encode(x).map(Json.fromString(_))))
                    case (Types.Output.Enum(c), Some(_)) =>
                      F.raiseError(s"enum type ${c.name} cannot have selections")
                    case (Types.Output.Scalar(c), None) =>
                      F.pure(PreparedLeaf(c.name, (x: Any) => Right(c.encoder(x))))
                    case (Types.Output.Scalar(c), Some(_)) =>
                      F.raiseError(s"scalar type ${c.name} cannot have selections")
                    case _ => ???
                  }

                prepF.map(p => PreparedDataField(field.name, resolve, p))
              }
          }
        case GQLParser.Selection.FragmentSpreadSelection(field) =>
          S.get.flatMap { state =>
            if (state.cycleSet.contains(field.fragmentName)) {
              F.raiseError(s"fragment by name ${field.fragmentName} is cyclic, discovered through path ${state.cycleSet.mkString(" -> ")}")
            } else {
              val fa: F[FragmentDefinition[G, Any]] =
                S.modify(s => s.copy(cycleSet = s.cycleSet + field.fragmentName)) >>
                  D.defer {
                    state.fragments.get(field.fragmentName) match {
                      case None => F.raiseError[FragmentDefinition[G, Any]](s"fragment by name ${field.fragmentName} not found")
                      case Some(FragmentAnalysis.Unevaluated(fd)) =>
                        prepareFragment(fd, schema, variableMap).flatTap { frag =>
                          S.modify(s => s.copy(fragments = s.fragments + (field.fragmentName -> FragmentAnalysis.Cached(frag))))
                        }
                      case Some(FragmentAnalysis.Cached(p)) => F.pure(p)
                    }
                  } <* S.modify(s => s.copy(cycleSet = s.cycleSet - field.fragmentName))

              fa.flatMap { fd =>
                (getPossibleTypes[F, G](typename, schema), getPossibleTypes[F, G](fd.typeCondition, schema)).tupled
                  .flatMap { case (parent, frag) =>
                    if ((frag & parent).isEmpty)
                      F.raiseError(s"fragment ${field.fragmentName} is not valid for type ${typename}, since the intersection of ${frag
                        .mkString(",")} and ${parent.mkString(",")} is empty")
                    else F.pure(PreparedFragmentReference(fd))
                  }
              }
            }
          }
        case GQLParser.Selection.InlineFragmentSelection(field) =>
          field.typeCondition match {
            case None => F.raiseError(s"inline fragment has no type condition")
            case Some(x) =>
              (getPossibleTypes[F, G](typename, schema), getPossibleTypes[F, G](x, schema)).tupled
                .flatMap[PreparedField[G, Any]] { case (parent, frag) =>
                  if ((frag & parent).isEmpty)
                    F.raiseError(
                      s"inline fragment spread on condition $x is not valid for type ${typename}, since the intersection of ${frag
                        .mkString(",")} and ${parent.mkString(",")} is empty"
                    )
                  else
                    schema.types(x) match {
                      case ot: Types.ObjectLike[G, Any] =>
                        def inputCheck(x: Any): Boolean = ot match {
                          case Types.Output.Interface(_, interfaces, _) => interfaces.exists(_.specify(x).isDefined)
                          case Types.Output.Object(_, _) => true
                        }

                        prepareSelections[F, G](
                          field.selectionSet,
                          x,
                          ot.fields,
                          schema,
                          variableMap
                        ).map(Selection(_)).map(s => PreparedInlineFragment(inputCheck, s))
                      case _ => F.raiseError(s"unsupported operation")
                    }
                }
          }
      }
  }

  def prepareFragment[F[_], G[_]](f: GQLParser.FragmentDefinition, schema: Types.Schema[G, _], variableMap: Map[String, Json])(implicit
      S: Stateful[F, AnalysisState[G]],
      F: MonadError[F, String],
      D: Defer[F]
  ): F[FragmentDefinition[G, Any]] =
    D.defer {
      schema.types.get(f.typeCnd) match {
        case None => F.raiseError(s"fragment ${f.name} references unknown type ${f.typeCnd}")
        case Some(ot: Types.ObjectLike[G, Any]) =>
          def inputCheck(x: Any): Boolean = ot match {
            case Types.Output.Interface(_, interfaces, _) => interfaces.exists(_.specify(x).isDefined)
            case Types.Output.Object(_, _) => true
          }

          prepareSelections[F, G](f.selectionSet, ot.name, ot.fields, schema, variableMap).attempt.flatMap {
            case Left(err) => F.raiseError(s"in fragment ${f.name}: $err")
            case Right(x)  => F.pure(FragmentDefinition(f.name, f.typeCnd, inputCheck, x))
          }
        case Some(ot) =>
          F.raiseError(s"fragment ${f.name} references scalar type ${ot.name}, but scalars are not allowed in fragments")
      }
    }

  final case class PreparedSchema[F[_], Q](query: Prepared[F, Q])

  // mapping from variable name to value and type
  final case class VariableMap(value: Map[String, (String, GQLParser.Type)]) extends AnyVal

  def prepare[F[_], Q](
      executabels: NonEmptyList[GQLParser.ExecutableDefinition],
      schema: Types.Schema[F, Q],
      variableMap: Map[String, Json]
  ): Either[String, FragmentDefinition[F, Any]] = {
    val (ops, frags) =
      executabels.toList.partitionEither {
        case GQLParser.ExecutableDefinition.Operation(op)  => Left(op)
        case GQLParser.ExecutableDefinition.Fragment(frag) => Right(frag)
      }

    // blabla check args first
    // ops.map {
    //   case Simple(_)                                            => ???
    //   case Detailed(tpe, name, None, _, _)                      => ???
    //   case Detailed(tpe, name, Some(variableDefinitions), _, _) => ???
    // }

    type G[A] = StateT[EitherT[Eval, String, *], AnalysisState[F], A]

    val o = prepareFragment[G, F](frags.head, schema, variableMap)

    o
      .runA(
        AnalysisState(
          frags.map { fd =>
            fd.name -> FragmentAnalysis.Unevaluated[F](fd)
          }.toMap,
          Set.empty
        )
      )
      .value
      .value
  }
}
