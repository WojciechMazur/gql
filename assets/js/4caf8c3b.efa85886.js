"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[271],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>d});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function l(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function o(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var s=a.createContext({}),c=function(e){var t=a.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):l(l({},t),e)),n},p=function(e){var t=c(e.components);return a.createElement(s.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,s=e.parentName,p=o(e,["components","mdxType","originalType","parentName"]),m=c(n),d=r,f=m["".concat(s,".").concat(d)]||m[d]||u[d]||i;return n?a.createElement(f,l(l({ref:t},p),{},{components:n})):a.createElement(f,l({ref:t},p))}));function d(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,l=new Array(i);l[0]=m;var o={};for(var s in t)hasOwnProperty.call(t,s)&&(o[s]=t[s]);o.originalType=e,o.mdxType="string"==typeof e?e:r,l[1]=o;for(var c=2;c<i;c++)l[c]=n[c];return a.createElement.apply(null,l)}return a.createElement.apply(null,n)}m.displayName="MDXCreateElement"},6915:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>s,contentTitle:()=>l,default:()=>u,frontMatter:()=>i,metadata:()=>o,toc:()=>c});var a=n(7462),r=(n(7294),n(3905));const i={title:"The DSL"},l=void 0,o={unversionedId:"schema/dsl",id:"schema/dsl",title:"The DSL",description:"The DSL consists of a set of smart constructors for the ast nodes of gql.",source:"@site/docs/schema/dsl.md",sourceDirName:"schema",slug:"/schema/dsl",permalink:"/gql/docs/schema/dsl",draft:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/schema/dsl.md",tags:[],version:"current",frontMatter:{title:"The DSL"},sidebar:"docs",previous:{title:"Input types",permalink:"/gql/docs/schema/input_types"},next:{title:"Resolvers",permalink:"/gql/docs/schema/resolvers"}},s={},c=[{value:"Fields",id:"fields",level:2},{value:"Value resolution",id:"value-resolution",level:3},{value:"Stream and Batch resolution",id:"stream-and-batch-resolution",level:3},{value:"Resolver composition",id:"resolver-composition",level:2},{value:"Unification instances",id:"unification-instances",level:2},{value:"Input types",id:"input-types",level:2},{value:"Other output structures",id:"other-output-structures",level:2}],p={toc:c};function u(e){let{components:t,...n}=e;return(0,r.kt)("wrapper",(0,a.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("p",null,"The DSL consists of a set of smart constructors for the ast nodes of gql.\nThe source code for the DSL is very easy to follow and as such, the best documentation is the source code itself :-)."),(0,r.kt)("h2",{id:"fields"},"Fields"),(0,r.kt)("p",null,"The simplest form of field construction comes from the ",(0,r.kt)("inlineCode",{parentName:"p"},"field")," smart constructor.\nIt simply lifts a resolver (and optionally an argument) into a field."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.data._\nimport cats.effect._\nimport cats.implicits._\nimport gql.dsl._\nimport gql.ast._\nimport gql.resolver._\n\ndef f = field(FallibleResolver[IO, String, String](s => IO.pure(s.rightIor)))\n\ndef intArg = arg[Int]("intArg")\nfield(intArg)(FallibleResolver[IO, (String, Int), String]{ case (s, i) => \n  IO.pure((s + i.toString()).rightIor)\n})\n// res0: Field[[A]IO[A], String, String, Int] = Field(\n//   args = NonEmptyArg(\n//     nec = Singleton(\n//       a = ArgValue(\n//         name = "intArg",\n//         input = cats.Later@22170e46,\n//         defaultValue = None,\n//         description = None\n//       )\n//     ),\n//     decode = gql.NonEmptyArg$$$Lambda$9602/0x0000000102a8d040@1ff7576d\n//   ),\n//   resolve = FallibleResolver(resolve = <function1>),\n//   output = cats.Later@47acecbe,\n//   description = None\n// )\n')),(0,r.kt)("h3",{id:"value-resolution"},"Value resolution"),(0,r.kt)("p",null,"Wrapping every field in a ",(0,r.kt)("inlineCode",{parentName:"p"},"field")," smart constructor and then defining the resolver seperately is a bit verbose.\nThere are smart constructors for three variants of field resolvers that lift the resolver function directly to a ",(0,r.kt)("inlineCode",{parentName:"p"},"Field"),"."),(0,r.kt)("p",null,"We must decide if the field is pure, an effect or a fallible effect:"),(0,r.kt)("admonition",{type:"note"},(0,r.kt)("p",{parentName:"admonition"},"The effect constructor is named ",(0,r.kt)("inlineCode",{parentName:"p"},"eff")," to avoid collisions with cats-effect.")),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'final case class Person(\n  name: String\n)\n\ntpe[IO, Person](\n  "Person",\n  "name" -> pure(_.name),\n  "nameEffect" -> eff(x => IO.delay(x.name)),\n  "nameFallible" -> fallible { x => \n    IO(Ior.both("some constructive error", x.name))\n  }\n)\n')),(0,r.kt)("p",null,"We can also include arguments in fields:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'def familyName = arg[String]("familyName")\n\ntpe[IO, Person](\n  "Person",\n  "name" -> pure(familyName)(_ + _),\n  "nameEffect" -> eff(familyName) { case (p, fn) => IO.delay(p.name + fn) },\n  "nameFallible" -> fallible(familyName) { case (p, fn) => \n    IO(Ior.both("some constructive error for $fn", p.name)) \n  }\n)\n')),(0,r.kt)("h3",{id:"stream-and-batch-resolution"},"Stream and Batch resolution"),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"StreamResolver"),"s can be constructed via the ",(0,r.kt)("inlineCode",{parentName:"p"},"stream")," and ",(0,r.kt)("inlineCode",{parentName:"p"},"streamFallible"),' smart constructors.\nBoth smart constructors are overloaded with a variant that take an explicit "next" resolver and a variant that does not.'),(0,r.kt)("p",null,"The ",(0,r.kt)("inlineCode",{parentName:"p"},"BatchResolver")," can be lifted into a ",(0,r.kt)("inlineCode",{parentName:"p"},"Field")," via the ",(0,r.kt)("inlineCode",{parentName:"p"},"field")," smart constructor."),(0,r.kt)("h2",{id:"resolver-composition"},"Resolver composition"),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"Resolver"),"s can be composed by using the ",(0,r.kt)("inlineCode",{parentName:"p"},"andThen")," method.\nThere are also several ",(0,r.kt)("inlineCode",{parentName:"p"},"map")," variants that combine ",(0,r.kt)("inlineCode",{parentName:"p"},"andThen")," with different types of resolvers:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'val r: Resolver[IO, Int, Int] = PureResolver[IO, Int, Int](x => x)\n\nr.andThen(PureResolver(_ + 1))\n\nr.map(_ + 1)\n\nr.evalMap(x => IO(x + 1))\n\nr.fallibleMap(x => IO(Ior.both("some constructive error", x + 1)))\n\nr.streamMap(x => fs2.Stream.iterate(x)(_ + 1).map(_.rightIor))\n')),(0,r.kt)("h2",{id:"unification-instances"},"Unification instances"),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"Union"),"s and ",(0,r.kt)("inlineCode",{parentName:"p"},"Interface"),"s require implementations of their type."),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"Union")," declares it's implementations on the ",(0,r.kt)("inlineCode",{parentName:"p"},"Union")," structure.\nHowever, ",(0,r.kt)("inlineCode",{parentName:"p"},"Interface")," implementations are declared on the types that implement the interface."),(0,r.kt)("p",null,"Before continuing, lets setup the environment."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"trait Vehicle { \n  def name: String\n}\nfinal case class Car(name: String) extends Vehicle\nfinal case class Boat(name: String) extends Vehicle\nfinal case class Truck(name: String) extends Vehicle\n\n")),(0,r.kt)("p",null,"For the ",(0,r.kt)("inlineCode",{parentName:"p"},"Union"),", variants can be declared using the ",(0,r.kt)("inlineCode",{parentName:"p"},"variant")," function, which takes a ",(0,r.kt)("inlineCode",{parentName:"p"},"PartialFunction")," from the unifying type to the implementation."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'implicit def car: Type[IO, Car] = ???\nimplicit def boat: Type[IO, Boat] = ???\nimplicit def truck: Type[IO, Truck] = ???\n\nunion[IO, Vehicle]("Vehicle")\n  .variant[Car] { case c: Car => c }\n  .variant[Boat] { case b: Boat => b }\n  .variant[Truck] { case t: Truck => t }\n')),(0,r.kt)("p",null,"A shorthand function exists, if the type of the variant is a subtype of the unifying type."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'union[IO, Vehicle]("Vehicle")\n  .subtype[Car] \n  .subtype[Boat] \n  .subtype[Truck] \n')),(0,r.kt)("p",null,"For an ",(0,r.kt)("inlineCode",{parentName:"p"},"Interface")," the same dsl exists, but is placed on the types that can implement the interface (a ",(0,r.kt)("inlineCode",{parentName:"p"},"Type")," or another ",(0,r.kt)("inlineCode",{parentName:"p"},"Interface"),")."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'implicit lazy val vehicle = interface[IO, Vehicle](\n  "Vehicle",\n  "name" -> pure(_.name)\n)\n\ntpe[IO, Car]("Car", "name" -> pure(_.name))\n  .implements[Vehicle]{ case c: Car => c }\n  \ntpe[IO, Boat]("Boat", "name" -> pure(_.name))\n  .subtypeOf[Vehicle]\n  \ntrait OtherVehicle extends Vehicle {\n  def weight: Int\n}\n\ninterface[IO, OtherVehicle](\n  "OtherVehicle",\n  "weight" -> pure(_.weight),\n  // Since OtherVehicle is a subtype of Vehicle\n  // we can directly embed the Vehicle fields\n  vehicle.fieldsList: _*\n).subtypeOf[Vehicle]\n')),(0,r.kt)("h2",{id:"input-types"},"Input types"),(0,r.kt)("p",null,"Review the ",(0,r.kt)("a",{parentName:"p",href:"./input_types"},"Input types")," section for more information."),(0,r.kt)("h2",{id:"other-output-structures"},"Other output structures"),(0,r.kt)("p",null,"Examples of other structures can be in the ",(0,r.kt)("a",{parentName:"p",href:"./output_types"},"Output types")," section."))}u.isMDXComponent=!0}}]);