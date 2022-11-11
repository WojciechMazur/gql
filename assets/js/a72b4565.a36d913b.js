"use strict";(self.webpackChunkwebsite=self.webpackChunkwebsite||[]).push([[961],{3905:(e,t,n)=>{n.d(t,{Zo:()=>p,kt:()=>f});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},i=Object.keys(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(a=0;a<i.length;a++)n=i[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var c=a.createContext({}),l=function(e){var t=a.useContext(c),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},p=function(e){var t=l(e.components);return a.createElement(c.Provider,{value:t},e.children)},u={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},m=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,i=e.originalType,c=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),m=l(n),f=r,d=m["".concat(c,".").concat(f)]||m[f]||u[f]||i;return n?a.createElement(d,o(o({ref:t},p),{},{components:n})):a.createElement(d,o({ref:t},p))}));function f(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var i=n.length,o=new Array(i);o[0]=m;var s={};for(var c in t)hasOwnProperty.call(t,c)&&(s[c]=t[c]);s.originalType=e,s.mdxType="string"==typeof e?e:r,o[1]=s;for(var l=2;l<i;l++)o[l]=n[l];return a.createElement.apply(null,o)}return a.createElement.apply(null,n)}m.displayName="MDXCreateElement"},9372:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>c,contentTitle:()=>o,default:()=>u,frontMatter:()=>i,metadata:()=>s,toc:()=>l});var a=n(7462),r=(n(7294),n(3905));const i={title:"Statistics"},o=void 0,s={unversionedId:"execution/statistics",id:"execution/statistics",title:"Statistics",description:"An instance of Statistics captures the runtime statistics of resolvers.",source:"@site/docs/execution/statistics.md",sourceDirName:"execution",slug:"/execution/statistics",permalink:"/gql/docs/execution/statistics",draft:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/execution/statistics.md",tags:[],version:"current",frontMatter:{title:"Statistics"},sidebar:"docs",previous:{title:"Planning",permalink:"/gql/docs/execution/planning"},next:{title:"Http4s",permalink:"/gql/docs/integrations/http4s"}},c={},l=[],p={toc:l};function u(e){let{components:t,...n}=e;return(0,r.kt)("wrapper",(0,a.Z)({},p,n,{components:t,mdxType:"MDXLayout"}),(0,r.kt)("p",null,"An instance of ",(0,r.kt)("inlineCode",{parentName:"p"},"Statistics")," captures the runtime statistics of resolvers.\nThe ",(0,r.kt)("inlineCode",{parentName:"p"},"Statistics")," structure uses an online linear regression algorithm to compute the relationship between batch size and execution time, such that memory usage is very minimal."),(0,r.kt)("p",null,"The ",(0,r.kt)("inlineCode",{parentName:"p"},"Statistics")," object records a mapping from ",(0,r.kt)("inlineCode",{parentName:"p"},"String")," to:"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"count"),": the number of points the regression contains."),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"meanX"),": the mean x coordinate of all the points."),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"meanY"),": the mean y coordinate of all the points."),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"varX"),": the variance of the x coordinates."),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("inlineCode",{parentName:"li"},"covXY"),": the covariance of the x and y coordinates.")),(0,r.kt)("p",null,"The ",(0,r.kt)("inlineCode",{parentName:"p"},"slope")," of the function can be computed as ",(0,r.kt)("inlineCode",{parentName:"p"},"covXY / varX")," and the ",(0,r.kt)("inlineCode",{parentName:"p"},"intercept")," as ",(0,r.kt)("inlineCode",{parentName:"p"},"meanY - slope * meanX"),"."),(0,r.kt)("p",null,"The ",(0,r.kt)("inlineCode",{parentName:"p"},"intercept")," acts the cost of one element while the ",(0,r.kt)("inlineCode",{parentName:"p"},"slope")," is the per element cost."),(0,r.kt)("p",null,"The ",(0,r.kt)("inlineCode",{parentName:"p"},"intercept")," is the important of the two, since it allows us to compare batch resolvers regardless of their average batch sizes."),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-scala"},'import cats.effect._\nimport gql._\nimport scala.concurrent.duration._\n\nimport cats.effect.unsafe.implicits.global\n\nStatistics[IO].flatMap{ stats =>\n  stats.updateStats("foo", 1.millis, 1) >>\n    stats.updateStats("foo", 2.millis, 4) >>\n    stats.updateStats("foo", 3.millis, 7) >>\n    stats.updateStats("foo", 4.millis, 10) >>\n    stats.getStats("foo")\n}.unsafeRunSync()\n// res0: Statistics.Stats = Stats(\n//   initialCost = 1000.0,\n//   extraElementCost = 333.3333333333333\n// )\n')))}u.isMDXComponent=!0}}]);