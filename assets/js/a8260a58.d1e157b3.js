"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[601],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>h});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function l(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var c=a.createContext({}),s=function(e){var t=a.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=s(e.components);return a.createElement(c.Provider,{value:t},e.children)},m={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},u=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,c=e.parentName,p=l(e,["components","mdxType","originalType","parentName"]),u=s(n),h=r,d=u["".concat(c,".").concat(h)]||u[h]||m[h]||i;return n?a.createElement(d,o(o({ref:t},p),{},{components:n})):a.createElement(d,o({ref:t},p))}));function h(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,o=new Array(i);o[0]=u;var l={};for(var c in t)hasOwnProperty.call(t,c)&&(l[c]=t[c]);l.originalType=e,l.mdxType="string"==typeof e?e:r,o[1]=l;for(var s=2;s<i;s++)o[s]=n[s];return a.createElement.apply(null,o)}return a.createElement.apply(null,n)}u.displayName="MDXCreateElement"},3951:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>c,contentTitle:()=>o,default:()=>m,frontMatter:()=>i,metadata:()=>l,toc:()=>s});var a=n(7462),r=(n(7294),n(3905));const i={title:"The schema"},o=void 0,l={unversionedId:"schema/schema",id:"schema/schema",title:"The schema",description:"SchemaShape",source:"@site/docs/schema/schema.md",sourceDirName:"schema",slug:"/schema/",permalink:"/gql/docs/schema/",draft:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/schema/schema.md",tags:[],version:"current",frontMatter:{title:"The schema"},sidebar:"docs",previous:{title:"Resolvers",permalink:"/gql/docs/schema/resolvers"},next:{title:"Context",permalink:"/gql/docs/schema/context"}},c={},s=[{value:"SchemaShape",id:"schemashape",level:2},{value:"Validation",id:"validation",level:3},{value:"Schema",id:"schema",level:2}],p={toc:s};function m(e){let{components:t,...n}=e;return(0,r.kt)("wrapper",(0,a.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("h2",{id:"schemashape"},"SchemaShape"),(0,r.kt)("p",null,"The ",(0,r.kt)("inlineCode",{parentName:"p"},"SchemaShape")," consists of the roots that make up your gql schema; A query, mutation and subscription type.\nThe ",(0,r.kt)("inlineCode",{parentName:"p"},"SchemaShape")," also contains extra types that should occur in the schema but are not neccesarilly discoverable through a walk of the ast."),(0,r.kt)("p",null,"The ",(0,r.kt)("inlineCode",{parentName:"p"},"SchemaShape")," also has derived information embedded in it.\nFor instance, one can render the schema:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.effect._\nimport cats.implicits._\nimport gql._\nimport gql.ast._\nimport gql.dsl._\n\ndef ss = SchemaShape[IO](\n  tpe[IO, Unit](\n    "Query",\n    "4hello" -> pure(_ => "world")\n  )\n)\n\nprintln(ss.render)\n// type Query {\n//   4hello: String!\n// }\n')),(0,r.kt)("h3",{id:"validation"},"Validation"),(0,r.kt)("p",null,"Validation of the shape is also derived information:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},"println(ss.validate)\n// Chain(invalid field name '4hello', must match /[_A-Za-z][_0-9A-Za-z]*/ at (Query).4hello)\n")),(0,r.kt)("p",null,"Running validation is completely optional, but is highly recommended.\nRunning queries against a unvalidated schema can have unforseen consequences."),(0,r.kt)("p",null,"For instance, here is a non-exhaustive list of things that can go wrong if not validated:"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Unforseen runtime errors if two definitions of a type has diverging field definitions."),(0,r.kt)("li",{parentName:"ul"},"Names do not respect the graphql spec."),(0,r.kt)("li",{parentName:"ul"},"Missing interface field implementations."),(0,r.kt)("li",{parentName:"ul"},"Invalid default value structure.")),(0,r.kt)("p",null,"Validation also reports other non-critical issues such as cases of ambiguity."),(0,r.kt)("p",null,"For instance, if a cyclic type is defined with ",(0,r.kt)("inlineCode",{parentName:"p"},"def"),", validation cannot determine if the type is truely valid.\nSolving this would require an infinite amount of time.\nAn exmaple follows:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'final case class A()\n\ndef cyclicType(i: Int): Type[IO, A] = {\n  if (i < 10000) tpe[IO, A](\n    "A",\n    "a" -> pure((_: A) => A())(cyclicType(i + 1))\n  )\n  else tpe[IO, A](\n    "A",\n    "a" -> pure(_ => "now I\'m a string :)")\n  )\n}\n\nimplicit lazy val cyclic: Type[IO, A] = cyclicType(0)\n\ndef recursiveSchema = SchemaShape[IO](\n  tpe[IO, Unit](\n    "Query",\n    "a" -> pure(_ => A())\n  )\n)\n\nrecursiveSchema.validate.toList.mkString("\\n")\n// res2: String = "cyclic type A is not reference equal use lazy val or `cats.Eval` to declare this type at (Query).a(A).a(A)"\n')),(0,r.kt)("p",null,"After ",(0,r.kt)("inlineCode",{parentName:"p"},"10000")," iterations the type is no longer unifyable."),(0,r.kt)("p",null,"One can also choose to simply ignore some of the validation errors:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'recursiveSchema.validate.filter{\n  case SchemaShape.Problem(SchemaShape.ValidationError.CyclicOutputType("A"), _) => false\n  case _ => true\n}\n// res3: cats.data.Chain[SchemaShape.Problem] = Chain()\n')),(0,r.kt)("admonition",{type:"info"},(0,r.kt)("p",{parentName:"admonition"},"Validation does not attempt structural equallity since this can have unforseen performance consequences."),(0,r.kt)("p",{parentName:"admonition"},"For instance, if the whole graph was defined with ",(0,r.kt)("inlineCode",{parentName:"p"},"def"),"s, one could very easily accedentally construct a case of exponential running time.")),(0,r.kt)("h2",{id:"schema"},"Schema"),(0,r.kt)("p",null,"A ",(0,r.kt)("inlineCode",{parentName:"p"},"Schema")," is a collection of some components that are required to execute a query.\nThe ",(0,r.kt)("inlineCode",{parentName:"p"},"Schema")," contains a ",(0,r.kt)("inlineCode",{parentName:"p"},"SchemaShape"),", a ",(0,r.kt)("inlineCode",{parentName:"p"},"Statistics")," instance, a query ",(0,r.kt)("inlineCode",{parentName:"p"},"Planner")," implementation and state regarding ",(0,r.kt)("inlineCode",{parentName:"p"},"BatchResolver")," implementations."),(0,r.kt)("admonition",{type:"tip"},(0,r.kt)("p",{parentName:"admonition"},"Check out the ",(0,r.kt)("a",{parentName:"p",href:"../execution/statistics"},"statistics section")," for more information on the ",(0,r.kt)("inlineCode",{parentName:"p"},"Statistics")," object."),(0,r.kt)("p",{parentName:"admonition"},"Also, check out the ",(0,r.kt)("a",{parentName:"p",href:"../execution/planning"},"planning section")," for more information on how the default query planner works."),(0,r.kt)("p",{parentName:"admonition"},"Finally, you can look in the ",(0,r.kt)("a",{parentName:"p",href:"./resolvers"},"resolver section")," for more information on ",(0,r.kt)("inlineCode",{parentName:"p"},"BatchResolver"),"s.")),(0,r.kt)("p",null,"The most powerful ",(0,r.kt)("inlineCode",{parentName:"p"},"Schema")," constructor ",(0,r.kt)("inlineCode",{parentName:"p"},"stateful"),", converts a ",(0,r.kt)("inlineCode",{parentName:"p"},"State[SchemaState[F], SchemaShape[F, Q, M, S]]")," to a ",(0,r.kt)("inlineCode",{parentName:"p"},"Schema[F, Q, M, S]"),"."))}m.isMDXComponent=!0}}]);