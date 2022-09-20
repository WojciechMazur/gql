"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[825],{3905:(e,t,n)=>{n.d(t,{Zo:()=>l,kt:()=>d});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function o(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?o(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):o(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function c(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)n=o[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var s=r.createContext({}),p=function(e){var t=r.useContext(s),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},l=function(e){var t=p(e.components);return r.createElement(s.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},m=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,o=e.originalType,s=e.parentName,l=c(e,["components","mdxType","originalType","parentName"]),m=p(n),d=a,f=m["".concat(s,".").concat(d)]||m[d]||u[d]||o;return n?r.createElement(f,i(i({ref:t},l),{},{components:n})):r.createElement(f,i({ref:t},l))}));function d(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var o=n.length,i=new Array(o);i[0]=m;var c={};for(var s in t)hasOwnProperty.call(t,s)&&(c[s]=t[s]);c.originalType=e,c.mdxType="string"==typeof e?e:a,i[1]=c;for(var p=2;p<o;p++)i[p]=n[p];return r.createElement.apply(null,i)}return r.createElement.apply(null,n)}m.displayName="MDXCreateElement"},5960:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>s,contentTitle:()=>i,default:()=>u,frontMatter:()=>o,metadata:()=>c,toc:()=>p});var r=n(7462),a=(n(7294),n(3905));const o={title:"Context"},i=void 0,c={unversionedId:"context",id:"context",title:"Context",description:"Many GraphQL implementations provide some method to pass query-wide parameters around in the graph.",source:"@site/docs/context.md",sourceDirName:".",slug:"/context",permalink:"/gql/docs/context",draft:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/context.md",tags:[],version:"current",frontMatter:{title:"Context"},sidebar:"docs",previous:{title:"Getting started",permalink:"/gql/docs/test"}},s={},p=[],l={toc:p};function u(e){let{components:t,...n}=e;return(0,a.kt)("wrapper",(0,r.Z)({},l,n,{components:t,mdxType:"MDXLayout"}),(0,a.kt)("p",null,"Many GraphQL implementations provide some method to pass query-wide parameters around in the graph.\ngql has no such concept, it is rather a by-product of being written in tagless style.\nWe can emulate context by using a ",(0,a.kt)("inlineCode",{parentName:"p"},"ReaderT"),"/",(0,a.kt)("inlineCode",{parentName:"p"},"Kleisli")," monad transformer from ",(0,a.kt)("inlineCode",{parentName:"p"},"cats"),".\nWriting ",(0,a.kt)("inlineCode",{parentName:"p"},"ReaderT"),"/",(0,a.kt)("inlineCode",{parentName:"p"},"Kleisli")," everywhere is tedious, instead consider opting for ",(0,a.kt)("inlineCode",{parentName:"p"},"cats.mtl.Ask"),":"),(0,a.kt)("pre",null,(0,a.kt)("code",{parentName:"pre",className:"language-scala"},'import gql._\nimport gql.dsl._\nimport gql.ast._\nimport cats.mtl.Ask\nimport cats._\nimport cats.data._\nimport cats.implicits._\nimport io.circe._\nimport cats.effect._\nimport cats.effect.unsafe.implicits.global\n\nfinal case class Context(\n  userId: String\n)\n\ndef getSchema[F[_]: Applicative](implicit A: Ask[F, Context]): Schema[F, Unit] = {\n  def schema: Schema[F, Unit] = Schema.simple[F, Unit](\n    tpe(\n      "Query",\n      "me" -> eff(_ => A.ask.map(_.userId))\n    )\n  )\n  \n  schema\n}\n\ntype G[A] = Kleisli[IO, Context, A]\ndef s = getSchema[G]\n\ndef query = """\n  query {\n    me\n  }\n"""\n  \ndef parsed = gql.parser.parse(query).toOption.get\n \nimplicit lazy val stats = \n  Statistics[IO].unsafeRunSync().mapK(Kleisli.liftK[IO, Context])\n\ndef queryResult = Execute.executor(parsed, s, Map.empty) match {\n  case Execute.ExecutorOutcome.Query(run) => run(()).map { case (_, output) => output } \n}\n\nqueryResult.run(Context("john_doe")).unsafeRunSync()\n// res0: JsonObject = object[me -> "john_doe"]\n')))}u.isMDXComponent=!0}}]);