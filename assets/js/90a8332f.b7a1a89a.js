"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[708],{3905:(e,n,t)=>{t.d(n,{Zo:()=>c,kt:()=>m});var a=t(7294);function i(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);n&&(a=a.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,a)}return t}function r(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){i(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,a,i=function(e,n){if(null==e)return{};var t,a,i={},o=Object.keys(e);for(a=0;a<o.length;a++)t=o[a],n.indexOf(t)>=0||(i[t]=e[t]);return i}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(a=0;a<o.length;a++)t=o[a],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(i[t]=e[t])}return i}var s=a.createContext({}),p=function(e){var n=a.useContext(s),t=n;return e&&(t="function"==typeof e?e(n):r(r({},n),e)),t},c=function(e){var n=p(e.components);return a.createElement(s.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return a.createElement(a.Fragment,{},n)}},u=a.forwardRef((function(e,n){var t=e.components,i=e.mdxType,o=e.originalType,s=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),u=p(t),m=i,h=u["".concat(s,".").concat(m)]||u[m]||d[m]||o;return t?a.createElement(h,r(r({ref:n},c),{},{components:t})):a.createElement(h,r({ref:n},c))}));function m(e,n){var t=arguments,i=n&&n.mdxType;if("string"==typeof e||i){var o=t.length,r=new Array(o);r[0]=u;var l={};for(var s in n)hasOwnProperty.call(n,s)&&(l[s]=n[s]);l.originalType=e,l.mdxType="string"==typeof e?e:i,r[1]=l;for(var p=2;p<o;p++)r[p]=t[p];return a.createElement.apply(null,r)}return a.createElement.apply(null,t)}u.displayName="MDXCreateElement"},271:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>s,contentTitle:()=>r,default:()=>d,frontMatter:()=>o,metadata:()=>l,toc:()=>p});var a=t(7462),i=(t(7294),t(3905));const o={title:"Output types"},r=void 0,l={unversionedId:"schema/output_types",id:"schema/output_types",title:"Output types",description:"An output type Out[F[_], A] is an ast node that can take some A as input and produce a graphql value in the effect F.",source:"@site/docs/schema/output_types.md",sourceDirName:"schema",slug:"/schema/output_types",permalink:"/gql/docs/schema/output_types",draft:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/schema/output_types.md",tags:[],version:"current",frontMatter:{title:"Output types"},sidebar:"docs",previous:{title:"Getting started",permalink:"/gql/docs/overview/getting_started"},next:{title:"Input types",permalink:"/gql/docs/schema/input_types"}},s={},p=[{value:"Scalar",id:"scalar",level:2},{value:"Enum",id:"enum",level:2},{value:"Field",id:"field",level:2},{value:"Type (object)",id:"type-object",level:2},{value:"Union",id:"union",level:2},{value:"Ad-hoc unions",id:"ad-hoc-unions",level:3},{value:"For the daring",id:"for-the-daring",level:3},{value:"Interface",id:"interface",level:2},{value:"A note on interface relationships",id:"a-note-on-interface-relationships",level:3},{value:"Unreachable types",id:"unreachable-types",level:2}],c={toc:p};function d(e){let{components:n,...t}=e;return(0,i.kt)("wrapper",(0,a.Z)({},c,t,{components:n,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"An output type ",(0,i.kt)("inlineCode",{parentName:"p"},"Out[F[_], A]")," is an ast node that can take some ",(0,i.kt)("inlineCode",{parentName:"p"},"A")," as input and produce a graphql value in the effect ",(0,i.kt)("inlineCode",{parentName:"p"},"F"),".\nOutput types act as continuations of their input types, such that a schema effectively is a tree of continuations.\nThe output types of gql are defined in ",(0,i.kt)("inlineCode",{parentName:"p"},"gql.ast")," and are named after their respective GraphQL types."),(0,i.kt)("admonition",{type:"note"},(0,i.kt)("p",{parentName:"admonition"},"Most examples use the ",(0,i.kt)("inlineCode",{parentName:"p"},"dsl")," to construct output types.\nThe types can naturally be constructed manually as well, but this can be verbose.")),(0,i.kt)("p",null,"Lets import the things we need: "),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},"import gql.ast._\nimport gql.resolver._\nimport gql.dsl._\nimport gql._\nimport cats._\nimport cats.data._\nimport cats.implicits._\nimport cats.effect._\n")),(0,i.kt)("h2",{id:"scalar"},"Scalar"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Scalar")," types are composed of a name, an encoder and a decoder.\nThe ",(0,i.kt)("inlineCode",{parentName:"p"},"Scalar")," type can encode ",(0,i.kt)("inlineCode",{parentName:"p"},"A => Value")," and decode ",(0,i.kt)("inlineCode",{parentName:"p"},"Value => Either[Error, A]"),".\nA ",(0,i.kt)("inlineCode",{parentName:"p"},"Value")," is a graphql value, which is a superset of json."),(0,i.kt)("p",null,"gql comes with a few predefined scalars, but you can also define your own.\nFor instance, the ",(0,i.kt)("inlineCode",{parentName:"p"},"ID")," type is defined for any ",(0,i.kt)("inlineCode",{parentName:"p"},"Scalar")," as follows:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'final case class ID[A](value: A)\n\nobject ID {\n  implicit def idTpe[F[_], A](implicit s: Scalar[F, A]): Scalar[F, ID[A]] =\n    s.imap(ID(_))(_.value)\n      .rename("ID")\n      .document(\n        """|The `ID` scalar type represents a unique identifier, often used to refetch an object or as key for a cache.\n           |The ID type appears in a JSON response as a String; however, it is not intended to be human-readable.\n           |When expected as an input type, any string (such as `\\"4\\"`) or integer (such as `4`) input value will be accepted as an ID."""".stripMargin\n      )\n}\n  \nimplicitly[Scalar[IO, ID[String]]]\n// res0: Scalar[IO, ID[String]] = Scalar(\n//   name = "ID",\n//   encoder = scala.Function1$$Lambda$10928/0x0000000102f53040@72f93dff,\n//   decoder = scala.Function1$$Lambda$9714/0x0000000102ba7840@10ad8492,\n//   description = Some(\n//     value = """The `ID` scalar type represents a unique identifier, often used to refetch an object or as key for a cache.\n// The ID type appears in a JSON response as a String; however, it is not intended to be human-readable.\n// When expected as an input type, any string (such as `\\"4\\"`) or integer (such as `4`) input value will be accepted as an ID.""""\n//   )\n// )\n')),(0,i.kt)("h2",{id:"enum"},"Enum"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Enum")," types, like ",(0,i.kt)("inlineCode",{parentName:"p"},"Scalar")," types, are terminal types that consist of a name and non-empty bi-directional mapping from a scala type to a ",(0,i.kt)("inlineCode",{parentName:"p"},"String"),":"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'sealed trait Color\nobject Color {\n  case object Red extends Color\n  case object Green extends Color\n  case object Blue extends Color\n}\n\nenumType[IO, Color](\n  "Color",\n  "RED" -> enumVal(Color.Red),\n  "GREEN" -> enumVal(Color.Green),\n  "BLUE" -> enumVal(Color.Blue)\n)\n')),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Enum")," types have no constraints on the values they can encode or decode, so they can in fact, be dynamically typed:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'final case class UntypedEnum(s: String)\n\nenumType[IO, UntypedEnum](\n  "UntypedEnum",\n  "FIRST" -> enumVal(UntypedEnum("FIRST"))\n)\n')),(0,i.kt)("admonition",{type:"caution"},(0,i.kt)("p",{parentName:"admonition"},"Encoding a value that has not been defined in the enum will result in a GraphQL error.\nTherefore, it is recommended to enumerate the image of the enum; only use ",(0,i.kt)("inlineCode",{parentName:"p"},"sealed trait"),"s")),(0,i.kt)("h2",{id:"field"},"Field"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Field")," is a type that represents a field in a graphql ",(0,i.kt)("inlineCode",{parentName:"p"},"type")," or ",(0,i.kt)("inlineCode",{parentName:"p"},"interface"),".\nA ",(0,i.kt)("inlineCode",{parentName:"p"},"Field[F, I, T, A]")," has arguments ",(0,i.kt)("inlineCode",{parentName:"p"},"Arg[A]"),", a continuation ",(0,i.kt)("inlineCode",{parentName:"p"},"Out[F, T]")," and a resolver that takes ",(0,i.kt)("inlineCode",{parentName:"p"},"(I, A)")," to ",(0,i.kt)("inlineCode",{parentName:"p"},"F[T]"),".\nField also lazily captures ",(0,i.kt)("inlineCode",{parentName:"p"},"Out[F, T]"),", to allow recursive types.\nThe ",(0,i.kt)("inlineCode",{parentName:"p"},"dsl")," lazily captures ",(0,i.kt)("inlineCode",{parentName:"p"},"Out[F, T]")," definitions in the implicit scope."),(0,i.kt)("admonition",{type:"tip"},(0,i.kt)("p",{parentName:"admonition"},"Check out the ",(0,i.kt)("a",{parentName:"p",href:"/gql/docs/schema/resolvers"},"resolver section")," for more info on how resolvers work.")),(0,i.kt)("h2",{id:"type-object"},"Type (object)"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Type")," is the gql equivalent of ",(0,i.kt)("inlineCode",{parentName:"p"},"type")," in GraphQL parlance.\nA ",(0,i.kt)("inlineCode",{parentName:"p"},"Type")," consists of a name and a non-empty list of fields."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'final case class Domain(\n  name: String,\n  amount: Int\n)\n\nType[IO, Domain](\n  "Domain",\n  NonEmptyList.of(\n    "name" -> Field[IO, Domain, String, Unit](\n      Applicative[Arg].unit,\n      PureResolver{ case (i, _) => i.name },\n      Eval.now(stringScalar)\n    ),\n    "amount" -> Field[IO, Domain, Int, Unit](\n      Applicative[Arg].unit, \n      PureResolver{ case (i, _) => i.amount },\n      Eval.now(intScalar)\n    )\n  ),\n  Nil\n)\n')),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Type"),"'s look very rough, but are significantly easier to define with the ",(0,i.kt)("inlineCode",{parentName:"p"},"dsl"),":"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'tpe[IO, Domain](\n  "Domain",\n  "name" -> pure(_.name),\n  "amount" -> pure(_.amount)\n)\n')),(0,i.kt)("admonition",{type:"tip"},(0,i.kt)("p",{parentName:"admonition"},"It is highly reccomended to define all ",(0,i.kt)("inlineCode",{parentName:"p"},"Type"),"s, ",(0,i.kt)("inlineCode",{parentName:"p"},"Union"),"s and ",(0,i.kt)("inlineCode",{parentName:"p"},"Interface"),"s as either ",(0,i.kt)("inlineCode",{parentName:"p"},"val")," or ",(0,i.kt)("inlineCode",{parentName:"p"},"lazy val"),".")),(0,i.kt)("h2",{id:"union"},"Union"),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Union")," types allow unification of arbitary types.\nThe ",(0,i.kt)("inlineCode",{parentName:"p"},"Union")," type defines a set of ",(0,i.kt)("inlineCode",{parentName:"p"},"PartialFunction"),"s that can specify the the type."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'sealed trait Animal\nfinal case class Dog(name: String) extends Animal\nfinal case class Cat(name: String) extends Animal\n\nimplicit lazy val dog = tpe[IO, Dog](\n  "Dog",\n  "name" -> pure(_.name)\n)\n\nimplicit lazy val cat = tpe[IO, Cat](\n  "Cat",\n  "name" -> pure(_.name)\n)\n\nunion[IO, Animal]("Animal")\n  .variant{ case x: Dog => x }\n  .subtype[Cat]\n')),(0,i.kt)("admonition",{type:"note"},(0,i.kt)("p",{parentName:"admonition"},"A curious reader might cosider the possibilty of using a total function form the unifying type to the subtypes.\nThis would also allow the scala compiler to catch non-exhaustive matches.\nThis is not possible, since the gql type needs to be available at the time of schema construction, and the specification function acts in query time.")),(0,i.kt)("admonition",{type:"caution"},(0,i.kt)("p",{parentName:"admonition"},"Defining instances for ",(0,i.kt)("inlineCode",{parentName:"p"},"Animal")," that are not referenced in the gql type is mostly safe, since any spread will simple give no fields.\nMost GraphQL clients also handle this case gracefully, for backwards compatibility reasons.\nThe exception is ",(0,i.kt)("inlineCode",{parentName:"p"},"__typename"),".\nIf the interpreter cannot find an instance of the value when querying for ",(0,i.kt)("inlineCode",{parentName:"p"},"__typename"),", a GraphQL error will be returned.")),(0,i.kt)("h3",{id:"ad-hoc-unions"},"Ad-hoc unions"),(0,i.kt)("p",null,"In the true spirit of unification, ",(0,i.kt)("inlineCode",{parentName:"p"},"Union")," types can be constructed in a more ad-hoc fashion:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'final case class Entity1(value: String)\nfinal case class Entity2(value: String)\n\nsealed trait Unification\nobject Unification {\n  final case class E1(value: Entity1) extends Unification\n  final case class E2(value: Entity2) extends Unification\n}\n\nimplicit lazy val entity1: Type[IO, Entity1] = ???\n\nimplicit lazy val entity2: Type[IO, Entity2] = ???\n\nunion[IO, Unification]("Unification")\n  .variant{ case Unification.E1(value) => value }\n  .variant{ case Unification.E2(value) => value }\n')),(0,i.kt)("h3",{id:"for-the-daring"},"For the daring"),(0,i.kt)("p",null,"Since the specify function is a ",(0,i.kt)("inlineCode",{parentName:"p"},"PartialFunction"),", it is indeed possible to have no unifying type:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'union[IO, Any]("AnyUnification")\n  .variant{ case x: Entity1 => x }\n  .variant{ case x: Entity2 => x }\n// res7: Union[IO, Any] = Union(\n//   name = "AnyUnification",\n//   types = NonEmptyList(\n//     head = Variant(tpe = cats.Later@5b3ce658),\n//     tail = List(Variant(tpe = cats.Later@104a4b3e))\n//   ),\n//   description = None\n// )\n')),(0,i.kt)("p",null,"And also complex routing logic:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'union[IO, Unification]("RoutedUnification")\n  .variant{ case Unification.E1(x) if x.value == "Jane" => x }\n  .variant{ \n    case Unification.E1(x) => Entity2(x.value)\n    case Unification.E2(x) => x\n  }\n// res8: Union[IO, Unification] = Union(\n//   name = "RoutedUnification",\n//   types = NonEmptyList(\n//     head = Variant(tpe = cats.Later@2b678a0d),\n//     tail = List(Variant(tpe = cats.Later@571202e7))\n//   ),\n//   description = None\n// )\n')),(0,i.kt)("h2",{id:"interface"},"Interface"),(0,i.kt)("p",null,"An interface is a ",(0,i.kt)("inlineCode",{parentName:"p"},"Type"),' that can be "implemented".'),(0,i.kt)("p",null,(0,i.kt)("inlineCode",{parentName:"p"},"Interface"),"s have fields like ",(0,i.kt)("inlineCode",{parentName:"p"},"Type"),"s and can also be implemented by other ",(0,i.kt)("inlineCode",{parentName:"p"},"Type"),"s and ",(0,i.kt)("inlineCode",{parentName:"p"},"Interface"),"s.\n",(0,i.kt)("inlineCode",{parentName:"p"},"Interface"),"s don't declare their implementations, but rather the implementations declare their interfaces."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'sealed trait Node {\n def id: String\n}\n\nfinal case class Person(\n  name: String,\n  id: String\n) extends Node\n\nfinal case class Company(\n  name: String,\n  id: String\n) extends Node\n  \nimplicit lazy val node = interface[IO, Node](\n  "Node",\n  "id" -> pure(x => ID(x.id))\n)\n\nlazy val person = tpe[IO, Person](\n  "Person",\n  "name" -> pure(_.name),\n  "id" -> pure(x => ID(x.id))\n).implements[Node]{ case x: Person => x }\n  \nlazy val company = tpe[IO, Company](\n  "Company",\n  "name" -> pure(_.name),\n)\n  .addFields(node.fieldsList: _*)\n  .subtypeOf[Node]\n')),(0,i.kt)("h3",{id:"a-note-on-interface-relationships"},"A note on interface relationships"),(0,i.kt)("admonition",{type:"info"},(0,i.kt)("p",{parentName:"admonition"},"This sub-section is a bit of a philosophical digression and can be skipped.")),(0,i.kt)("p",null,"The nature of the ",(0,i.kt)("inlineCode",{parentName:"p"},"Interface"),' type unfortunately causes some complications.\nSince a relation goes from implementation to interface, cases of ambiguity can arise of what interface to consider the "truth".\nSchema validation will catch such cases, but it can still feel like a somewhat arbitrary limitation.'),(0,i.kt)("p",null,"One could argue that the relation could simple be inverted, like unions, but alas such an endeavour has another consequence.\nConceptually an interface is defined most generally (in a core library or a most general purpose module), where implementations occur in more specific places.\nInverting the relationships of the interface would mean that the interface would have to be defined in the most specific place instead of the most general.\nThat is, inverting the arrows (relationships) of an interface, produces a union instead (with some extra features such as fields)."),(0,i.kt)("p",null,"Now we must define the scala type for the interface in the most general place, but the ",(0,i.kt)("inlineCode",{parentName:"p"},"Interface")," in the most specific?\nConnecting such a graph requires significant effort (exploitation of some laziness) and as such is not the chosen approach."),(0,i.kt)("h2",{id:"unreachable-types"},"Unreachable types"),(0,i.kt)("p",null,"gql discovers types by traversing the schema types.\nThis also means that even if you have a type declared it must occur in the ast to be respected."),(0,i.kt)("p",null,"You might want to declare types that are not yet queryable.\nOr maybe you only expose an interface, but not the implementing types, thus the implementations won't be discovered."),(0,i.kt)("p",null,'The schema lets you declare "extra" types that should occur in introspection, rendering and evaluation (if possible):'),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-scala"},'def getNode: Node = Company("gql", "1")\n\ndef shape = SchemaShape[IO](tpe[IO, Unit]("Query", "node" -> pure(_ => getNode)))\n\nprintln(shape.render)\n// type Query {\n//   node: Node!\n// }\n// \n// interface Node {\n//   id: ID!\n// }\n\ndef withCompany = shape.addOutputTypes(company)\n\nprintln(withCompany.render)\n// type Company implements Node {\n//   name: String!\n//   id: ID!\n// }\n// \n// interface Node {\n//   id: ID!\n// }\n// \n// type Query {\n//   node: Node!\n// }\n\nprintln(withCompany.addOutputTypes(person).render)\n// type Company implements Node {\n//   name: String!\n//   id: ID!\n// }\n// \n// interface Node {\n//   id: ID!\n// }\n// \n// type Query {\n//   node: Node!\n// }\n// \n// type Person implements Node {\n//   name: String!\n//   id: ID!\n// }\n')))}d.isMDXComponent=!0}}]);