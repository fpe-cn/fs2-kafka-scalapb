val confluentVersion = "6.2.0"

val scalapbVersion = "0.11.4"

val scala212 = "2.12.14"

val scala213 = "2.13.6"

val scala3 = "3.0.0"

lazy val core = project
  .in(file("."))
  .settings(
    moduleName := "fs2-kafka-scalapb",
    name := moduleName.value,
    dependencySettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.github.fd4s" %% "fs2-kafka" % "2.1.0",
        "io.confluent" % "kafka-protobuf-serializer" % confluentVersion,
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion
      )
    ),
    publishSettings,
    scalaSettings,
    testSettings
  )

lazy val docs = project
  .in(file("docs"))
  .settings(
    moduleName := "fs2-kafka-docs",
    name := moduleName.value,
    dependencySettings,
    noPublishSettings,
    scalaSettings,
    mdocSettings,
    buildInfoSettings
  )
  .dependsOn(core)
  .enablePlugins(BuildInfoPlugin, DocusaurusPlugin, MdocPlugin, ScalaUnidocPlugin)

lazy val dependencySettings = Seq(
  resolvers += "confluent" at "https://packages.confluent.io/maven/",
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.typelevel" %% "discipline-scalatest" % "2.1.5"
  ).map(_ % Test),
  libraryDependencies ++= {
    if (scalaVersion.value.startsWith("3")) Nil
    else
      Seq(
        compilerPlugin(
          ("org.typelevel" %% "kind-projector" % "0.13.0")
            .cross(CrossVersion.full)
        )
      )
  },
  pomPostProcess := { (node: xml.Node) =>
    new xml.transform.RuleTransformer(new xml.transform.RewriteRule {
      def scopedDependency(e: xml.Elem): Boolean =
        e.label == "dependency" && e.child.exists(_.label == "scope")

      override def transform(node: xml.Node): xml.NodeSeq =
        node match {
          case e: xml.Elem if scopedDependency(e) => Nil
          case _                                  => Seq(node)
        }
    }).transform(node).head
  }
)

lazy val mdocSettings = Seq(
  mdoc := (Compile / run).evaluated,
  scalacOptions --= Seq("-Xfatal-warnings", "-Ywarn-unused"),
  crossScalaVersions := Seq(scalaVersion.value),
  ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(core),
  ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
  cleanFiles += (ScalaUnidoc / unidoc / target).value,
  docusaurusCreateSite := docusaurusCreateSite
    .dependsOn(Compile / unidoc)
    .dependsOn(ThisBuild / updateSiteVariables)
    .value,
  docusaurusPublishGhpages :=
    docusaurusPublishGhpages
      .dependsOn(Compile / unidoc)
      .dependsOn(ThisBuild / updateSiteVariables)
      .value,
  // format: off
  ScalaUnidoc / unidoc / scalacOptions ++= Seq(
    "-doc-source-url", s"https://github.com/fpe-cn/fs2-kafka-scalapb/tree/v${(ThisBuild / latestVersion).value}€{FILE_PATH}.scala",
    "-sourcepath", (LocalRootProject / baseDirectory).value.getAbsolutePath,
    "-doc-title", "FS2 Kafka ScalaPB",
    "-doc-version", s"v${(ThisBuild / latestVersion).value}"
  )
  // format: on
)

lazy val buildInfoSettings = Seq(
  buildInfoPackage := "fs2.kafka.build",
  buildInfoObject := "info",
  buildInfoKeys := Seq[BuildInfoKey](
    scalaVersion,
    scalacOptions,
    sourceDirectory,
    ThisBuild / latestVersion,
    BuildInfoKey.map(ThisBuild / version) {
      case (_, v) => "latestSnapshotVersion" -> v
    },
    BuildInfoKey.map(core / moduleName) {
      case (k, v) => "core" ++ k.capitalize -> v
    },
    BuildInfoKey.map(core / crossScalaVersions) {
      case (k, v) => "core" ++ k.capitalize -> v
    },
    LocalRootProject / organization,
    core / crossScalaVersions,
    BuildInfoKey("scalapbVersion" -> scalapbVersion),
    BuildInfoKey("confluentVersion" -> confluentVersion)
  )
)

lazy val metadataSettings = Seq(
  organization := "fr.fpe"
)

ThisBuild / githubWorkflowTargetBranches := Seq("main")

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("ci")),
  WorkflowStep.Sbt(List("docs/run"), cond = Some(s"matrix.scala == '$scala213'"))
)

ThisBuild / githubWorkflowArtifactUpload := false

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release", "docs/docusaurusPublishGhpages"),
    env = Map(
      "GIT_DEPLOY_KEY" -> "${{ secrets.GIT_DEPLOY_KEY }}",
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

lazy val publishSettings =
  metadataSettings ++ Seq(
    Test / publishArtifact := false,
    pomIncludeRepository := (_ => false),
    homepage := Some(url("https://fpe-cn.github.io/fs2-kafka-scalapb")),
    licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    startYear := Some(2021),
    headerLicense := Some(
      de.heikoseeberger.sbtheader.License.ALv2(
        s"${startYear.value.get}-${java.time.Year.now}",
        "Financiere des Paiements Electroniques",
        HeaderLicenseStyle.SpdxSyntax
      )
    ),
    headerSources / excludeFilter := HiddenFileFilter
  )

lazy val noPublishSettings =
  publishSettings ++ Seq(
    publish / skip := true,
    publishArtifact := false
  )

ThisBuild / scalaVersion := scala213
ThisBuild / crossScalaVersions := Seq(scala212, scala213, scala3)

lazy val scalaSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked"
  ) ++ (
    if (scalaVersion.value.startsWith("2.13"))
      Seq(
        "-language:higherKinds",
        "-Xlint",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Ywarn-unused",
        "-Xfatal-warnings"
      )
    else if (scalaVersion.value.startsWith("2.12"))
      Seq(
        "-language:higherKinds",
        "-Xlint",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Ywarn-unused",
        "-Ypartial-unification",
        "-Xfatal-warnings"
      )
    else
      Seq(
        "-Ykind-projector",
        "-source:3.0-migration",
        "-Xignore-scala2-macros"
      )
  ),
  Compile / doc / scalacOptions += "-nowarn", // workaround for https://github.com/scala/bug/issues/12007
  Compile / console / scalacOptions --= Seq("-Xlint", "-Ywarn-unused"),
  Test / console / scalacOptions := (Compile / console / scalacOptions).value,
  Compile / unmanagedSourceDirectories ++=
    Seq(
      baseDirectory.value / "src" / "main" / {
        if (scalaVersion.value.startsWith("2.12"))
          "scala-2.12"
        else "scala-2.13+"
      }
    ),
  Test / fork := true
)

lazy val testSettings = Seq(
  Test / logBuffered := false,
  Test / parallelExecution := false,
  Test / testOptions += Tests.Argument("-oDF")
)

def minorVersion(version: String): String = {
  val (major, minor) =
    CrossVersion.partialVersion(version).get
  s"$major.$minor"
}

val latestVersion = settingKey[String]("Latest released version")
ThisBuild / latestVersion := (ThisBuild / version).value

val updateSiteVariables = taskKey[Unit]("Update site variables")
ThisBuild / updateSiteVariables := {
  val file =
    (LocalRootProject / baseDirectory).value / "website" / "variables.js"

  val variables =
    Map[String, String](
      "organization" -> (LocalRootProject / organization).value,
      "coreModuleName" -> (core / moduleName).value,
      "latestVersion" -> (ThisBuild / latestVersion).value,
      "scalaPublishVersions" -> {
        val minorVersions = (core / crossScalaVersions).value.map(minorVersion)
        if (minorVersions.size <= 2) minorVersions.mkString(" and ")
        else minorVersions.init.mkString(", ") ++ " and " ++ minorVersions.last
      }
    )

  val fileHeader =
    "// Generated by sbt. Do not edit directly."

  val fileContents =
    variables.toList
      .sortBy { case (key, _) => key }
      .map { case (key, value) => s"  $key: '$value'" }
      .mkString(s"$fileHeader\nmodule.exports = {\n", ",\n", "\n};\n")

  IO.write(file, fileContents)
}

def addCommandsAlias(name: String, values: List[String]) =
  addCommandAlias(name, values.mkString(";", ";", ""))

addCommandsAlias(
  "validate",
  List(
    "+clean",
    "+test",
    "+mimaReportBinaryIssues",
    "+scalafmtCheck",
    "scalafmtSbtCheck",
    "+headerCheck",
    "+doc",
    "docs/run"
  )
)

addCommandsAlias(
  "ci",
  List(
    "clean",
    "test",
    "scalafmtCheck",
    "scalafmtSbtCheck",
    "headerCheck",
    "doc"
  )
)
