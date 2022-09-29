"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[454],{3905:(e,n,r)=>{r.d(n,{Zo:()=>p,kt:()=>f});var t=r(7294);function a(e,n,r){return n in e?Object.defineProperty(e,n,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[n]=r,e}function i(e,n){var r=Object.keys(e);if(Object.getOwnPropertySymbols){var t=Object.getOwnPropertySymbols(e);n&&(t=t.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),r.push.apply(r,t)}return r}function l(e){for(var n=1;n<arguments.length;n++){var r=null!=arguments[n]?arguments[n]:{};n%2?i(Object(r),!0).forEach((function(n){a(e,n,r[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(r)):i(Object(r)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(r,n))}))}return e}function o(e,n){if(null==e)return{};var r,t,a=function(e,n){if(null==e)return{};var r,t,a={},i=Object.keys(e);for(t=0;t<i.length;t++)r=i[t],n.indexOf(r)>=0||(a[r]=e[r]);return a}(e,n);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(t=0;t<i.length;t++)r=i[t],n.indexOf(r)>=0||Object.prototype.propertyIsEnumerable.call(e,r)&&(a[r]=e[r])}return a}var s=t.createContext({}),c=function(e){var n=t.useContext(s),r=n;return e&&(r="function"==typeof e?e(n):l(l({},n),e)),r},p=function(e){var n=c(e.components);return t.createElement(s.Provider,{value:n},e.children)},u={inlineCode:"code",wrapper:function(e){var n=e.children;return t.createElement(t.Fragment,{},n)}},d=t.forwardRef((function(e,n){var r=e.components,a=e.mdxType,i=e.originalType,s=e.parentName,p=o(e,["components","mdxType","originalType","parentName"]),d=c(r),f=a,g=d["".concat(s,".").concat(f)]||d[f]||u[f]||i;return r?t.createElement(g,l(l({ref:n},p),{},{components:r})):t.createElement(g,l({ref:n},p))}));function f(e,n){var r=arguments,a=n&&n.mdxType;if("string"==typeof e||a){var i=r.length,l=new Array(i);l[0]=d;var o={};for(var s in n)hasOwnProperty.call(n,s)&&(o[s]=n[s]);o.originalType=e,o.mdxType="string"==typeof e?e:a,l[1]=o;for(var c=2;c<i;c++)l[c]=r[c];return t.createElement.apply(null,l)}return t.createElement.apply(null,r)}d.displayName="MDXCreateElement"},8924:(e,n,r)=>{r.r(n),r.d(n,{assets:()=>s,contentTitle:()=>l,default:()=>u,frontMatter:()=>i,metadata:()=>o,toc:()=>c});var t=r(7462),a=(r(7294),r(3905));const i={title:"Error handling"},l=void 0,o={unversionedId:"schema/error_handling",id:"schema/error_handling",title:"Error handling",description:"There are different types of errors in gql.",source:"@site/docs/schema/error_handling.md",sourceDirName:"schema",slug:"/schema/error_handling",permalink:"/gql/docs/schema/error_handling",draft:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/schema/error_handling.md",tags:[],version:"current",frontMatter:{title:"Error handling"},sidebar:"docs",previous:{title:"Context",permalink:"/gql/docs/schema/context"},next:{title:"Digraph philosophy",permalink:"/gql/docs/schema/graph_philosophy"}},s={},c=[{value:"Execution",id:"execution",level:2},{value:"Examples",id:"examples",level:2},{value:"Exception trick",id:"exception-trick",level:3}],p={toc:c};function u(e){let{components:n,...i}=e;return(0,a.kt)("wrapper",(0,t.Z)({},p,i,{components:n,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"There are different types of errors in gql."),(0,a.kt)("ul",null,(0,a.kt)("li",{parentName:"ul"},"Schema validation errors, which should be caught in development.\nThese are for instance caused by duplicate field names or invalid typenames."),(0,a.kt)("li",{parentName:"ul"},"Query preparation errors, which are errors caused by invalid queries."),(0,a.kt)("li",{parentName:"ul"},"Execuion errors. These are errors that occur during query evaluation, caused by resolvers that fail.")),(0,a.kt)("h2",{id:"execution"},"Execution"),(0,a.kt)("p",null,"Error handling in gql can be performed in two ways, it can be returned explicitly or raised in ",(0,a.kt)("inlineCode",{parentName:"p"},"F"),".\nFor instance, the ",(0,a.kt)("inlineCode",{parentName:"p"},"EffectResolver[F, I, A]")," wraps the function ",(0,a.kt)("inlineCode",{parentName:"p"},"I => F[Ior[String, A]]"),"."),(0,a.kt)("h2",{id:"examples"},"Examples"),(0,a.kt)("p",null,"Let's setup the scene:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'import gql.ast._\nimport gql.dsl._\nimport gql._\nimport cats.implicits._\nimport cats.data._\nimport cats.effect._\nimport cats.effect.unsafe.implicits.global\n  \ndef multifailSchema = \n  tpe[IO, Unit](\n    "Query", \n    "field" -> fallible(arg[Int]("i", Some(10))){ \n      case (_, 0) => IO.pure(Ior.left("fail gracefully"))\n      case (_, 1) => IO.raiseError(new Exception("fail hard"))\n      case (_, i) => IO.pure(Ior.right(i))\n    }\n  )\n\ndef go(query: String, tpe: Type[IO, Unit] = multifailSchema) = \n  Schema.query(tpe).flatMap { sch =>\n    sch.assemble(query, variables = Map.empty)\n      .traverse { \n        case Executable.Query(run) => \n          run(()).map{x => println(x.errors);x.asGraphQL }\n        case Executable.ValidationError(msg) =>\n          println(msg)\n          IO.pure(msg.asGraphQL)\n      }\n  }.unsafeRunSync()\n  \ngo("query { field }")\n// Chain()\n// res0: Either[parser.package.ParseError, io.circe.JsonObject] = Right(\n//   value = object[data -> {\n//   "field" : 10\n// }]\n// )\n')),(0,a.kt)("p",null,"A query can fail gracefully by returning ",(0,a.kt)("inlineCode",{parentName:"p"},"Ior.left"),":"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'go("query { field(i: 0) }")\n// Chain(EffectResolution(CursorGroup(Cursor(Chain()),Cursor(Chain(Field(1,field))),1),Right(fail gracefully),()))\n// res1: Either[parser.package.ParseError, io.circe.JsonObject] = Right(\n//   value = object[errors -> [\n//   [\n//     {\n//       "message" : "fail gracefully",\n//       "path" : [\n//         "field"\n//       ]\n//     }\n//   ]\n// ],data -> {\n//   "field" : null\n// }]\n// )\n')),(0,a.kt)("p",null,"A query can fail hard by raising an exception:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'go("query { field(i: 1) }")\n// Chain(EffectResolution(CursorGroup(Cursor(Chain()),Cursor(Chain(Field(1,field))),1),Left(java.lang.Exception: fail hard),()))\n// res2: Either[parser.package.ParseError, io.circe.JsonObject] = Right(\n//   value = object[errors -> [\n//   [\n//     {\n//       "message" : "internal error",\n//       "path" : [\n//         "field"\n//       ]\n//     }\n//   ]\n// ],data -> {\n//   "field" : null\n// }]\n// )\n')),(0,a.kt)("p",null,"A query can also fail before even evaluating the query:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'go("query { nonExisting }")\n// PositionalError(PrepCursor(List(nonExisting)),List(Caret(0,20,20)),unknown field name nonExisting)\n// res3: Either[parser.package.ParseError, io.circe.JsonObject] = Right(\n//   value = object[message -> "unknown field name nonExisting",locations -> [\n//   {\n//     "line" : 0,\n//     "column" : 20\n//   }\n// ],path -> [\n//   "nonExisting"\n// ]]\n// )\n')),(0,a.kt)("p",null,"And finally, it can fail if it isn't parsable:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'def largerQuery = """\n  query {\n    field1\n    field2(test: 42)\n  }\n  \n  fragment test on Test {\n    -value1\n    value2 \n  }\n"""\n\ngo(largerQuery).leftMap(_.prettyError.value)\n// res4: Either[String, io.circe.JsonObject] = Left(\n//   value = """failed at offset 80 on line 7 with code 45\n// one of "..."\n// in char in range A to Z (code 65 to 90)\n// in char in range _ to _ (code 95 to 95)\n// in char in range a to z (code 97 to 122)\n// in query:\n// | \n// |   query {\n// |     field1\n// |     field2(test: 42)\n// |   }\n// |   \n// |   fragment test on Test {\n// |     -value1\n// | >^^^^^^^ line:7 code:45\n// |     value2 \n// |   }\n// | """\n// )\n')),(0,a.kt)("p",null,"Parser errors also look nice in ANSI terminals:"),(0,a.kt)("p",null,(0,a.kt)("img",{alt:"Terminal output",src:r(403).Z,width:"350",height:"329"})),(0,a.kt)("h3",{id:"exception-trick"},"Exception trick"),(0,a.kt)("p",null,"If for whatever reason you wish to pass information through exceptions, that is also possible:"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'final case class MyException(msg: String, data: Int) extends Exception(msg)\n\nval res = \n  Schema.query(\n    tpe[IO, Unit](\n      "Query",\n      "field" -> eff(_ => IO.raiseError[String](MyException("fail hard", 42)))\n    )\n  ).flatMap { sch =>\n    sch.assemble("query { field } ", variables = Map.empty)\n      .traverse { case Executable.Query(run) => run(()) }\n  }.unsafeRunSync()\n// res: Either[parser.package.ParseError, QueryResult] = Right(\n//   value = QueryResult(\n//     errors = Singleton(\n//       a = EffectResolution(\n//         path = CursorGroup(\n//           startPosition = Cursor(path = Chain()),\n//           relativePath = Cursor(\n//             path = Singleton(a = Field(id = 1, name = "field"))\n//           ),\n//           groupId = 1\n//         ),\n//         error = Left(value = MyException(msg = "fail hard", data = 42)),\n//         input = ()\n//       )\n//     ),\n//     data = object[field -> null]\n//   )\n// )\n  \nres.toOption.flatMap(_.errors.headOption).flatMap(_.exception) match {\n  case Some(MyException(_, data)) => println(s"Got data: $data")\n  case _ => println("No data")\n}\n// Got data: 42\n')))}u.isMDXComponent=!0},403:(e,n,r)=>{r.d(n,{Z:()=>t});const t=r.p+"assets/images/error_image-7805f49e8b21d536040a6e281835df41.png"}}]);