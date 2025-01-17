val scala213Version = "2.13.9"

ThisBuild / scalaVersion := scala213Version
ThisBuild / crossScalaVersions := Seq(scala213Version, "3.2.2")
ThisBuild / organization := "io.github.valdemargr"

ThisBuild / tlBaseVersion := "0.1"
ThisBuild / tlCiHeaderCheck := false
ThisBuild / tlCiDocCheck := false
ThisBuild / tlCiScalafmtCheck := false
ThisBuild / tlUntaggedAreSnapshots := false

ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / developers := List(
  Developer("valdemargr", "Valdemar Grange", "randomvald0069@gmail.com", url("https://github.com/valdemargr"))
)
ThisBuild / headerLicense := Some(HeaderLicense.Custom("Copyright (c) 2021 Valdemar Grange"))
ThisBuild / headerEmptyLine := false

ThisBuild / githubWorkflowAddedJobs +=
  WorkflowJob(
    id = "docs",
    name = "Run mdoc docs",
    scalas = List(scala213Version),
    steps = WorkflowStep.Checkout ::
      WorkflowStep.SetupJava(githubWorkflowJavaVersions.value.toList) ++
      githubWorkflowGeneratedCacheSteps.value ++
      List(
        WorkflowStep.Sbt(List("docs/mdoc")),
        WorkflowStep.Use(
          UseRef.Public("actions", "setup-node", "v3"),
          params = Map("node-version" -> "18")
        ),
        WorkflowStep.Run(List("cd website && yarn install")),
        WorkflowStep.Run(
          List(
            "git config --global user.name ValdemarGr",
            "git config --global user.email randomvald0069@gmail.com",
            "cd website && yarn deploy"
          ),
          env = Map(
            "GIT_USER" -> "valdemargr",
            "GIT_PASS" -> "${{ secrets.GITHUB_TOKEN }}"
          )
        )
      ),
    cond = Some("""github.event_name != 'pull_request' && (startsWith(github.ref, 'refs/tags/v') || github.ref == 'refs/heads/main')""")
  )

lazy val sharedSettings = Seq(
  organization := "io.github.valdemargr",
  organizationName := "Valdemar Grange",
  autoCompilerPlugins := true,
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-effect" % "3.3.14",
    "org.typelevel" %% "cats-collections-core" % "0.9.4",
    "org.typelevel" %% "cats-mtl" % "1.3.0",
    "co.fs2" %% "fs2-core" % "3.2.14",
    "co.fs2" %% "fs2-io" % "3.2.14",
    "org.typelevel" %% "cats-parse" % "0.3.8",
    "io.circe" %% "circe-core" % "0.14.3",
    "io.circe" %% "circe-generic" % "0.14.3",
    "io.circe" %% "circe-parser" % "0.14.3",
    "org.typelevel" %% "paiges-core" % "0.4.2",
    "org.scalameta" %% "munit" % "1.0.0-M6" % Test,
    "org.typelevel" %% "munit-cats-effect" % "2.0.0-M3" % Test
  )
)

lazy val core = project
  .in(file("modules/core"))
  .settings(sharedSettings)
  .settings(name := "gql-core"/*, tlFatalWarnings := true*/)

lazy val natchez = project
  .in(file("modules/natchez"))
  .settings(sharedSettings)
  .settings(
    name := "gql-natchez",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "natchez-core" % "0.1.4",
      "org.tpolecat" %% "natchez-noop" % "0.1.4"
    )
  )
  .dependsOn(core)

lazy val graphqlWs = project
  .in(file("modules/graphql-ws"))
  .settings(sharedSettings)
  .settings(name := "gql-graphqlws")
  .dependsOn(core)

lazy val goi = project
  .in(file("modules/goi"))
  .settings(sharedSettings)
  .settings(
    name := "gql-goi",
    libraryDependencies ++= Seq("com.beachape" %% "enumeratum" % "1.7.2")
  )
  .dependsOn(core)
  .enablePlugins(NoPublishPlugin)

lazy val http4s = project
  .in(file("modules/http4s"))
  .dependsOn(core % "compile->compile;test->test")
  .dependsOn(graphqlWs)
  .settings(sharedSettings)
  .settings(
    name := "gql-http4s",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-server" % "1.0.0-M36",
      "org.http4s" %% "http4s-blaze-server" % "1.0.0-M36",
      "org.http4s" %% "http4s-circe" % "1.0.0-M36",
      "org.http4s" %% "http4s-dsl" % "1.0.0-M36",
      "org.http4s" %% "http4s-client" % "1.0.0-M36" % Test
    )
  )

lazy val mdocExt = project
  .in(file("modules/mdoc-ext"))
  .settings(sharedSettings)
  .enablePlugins(NoPublishPlugin)

lazy val docs = project
  .in(file("modules/docs"))
  .settings(
    moduleName := "gql-docs",
    mdocOut := file("website/docs"),
    mdocVariables ++= Map(
      "VERSION" -> tlLatestVersion.value.getOrElse(version.value)
    ),
    libraryDependencies ++= Seq(
      "com.47deg" %% "fetch" % "3.1.0"
    ),
    tlFatalWarnings := false
  )
  .dependsOn(core % "compile->compile;compile->test")
  .dependsOn(http4s)
  .dependsOn(graphqlWs)
  /* .dependsOn(goi) */
  .dependsOn(mdocExt)
  .enablePlugins(MdocPlugin, DocusaurusPlugin, NoPublishPlugin)
