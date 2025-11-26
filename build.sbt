import Dependencies._
import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._
import sbtrelease.ReleaseStateTransformations._

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / scalaVersion := "2.13.18"
ThisBuild / organization := "io.github.novakov-alexey"

lazy val root = (project in file("."))
  .settings(
    name := "kerberos-operator2",
    addCompilerPlugin(betterMonadicFor),
    libraryDependencies ++= Seq(
      kubernetesClient,
      circeExtra,
      circeCore,
      pureConfig,
      logbackClassic,
      scalaTest % Test
    ),
    dockerBaseImage := "eclipse-temurin:11",
    Docker / dockerRepository := Some("alexeyn"),
    Universal / javaOptions ++= Seq(
      "-Dlogback.configurationFile=/opt/conf/logback.xml"
    ),
    Universal / mappings ++= directory("src/main/resources"),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoOptions += BuildInfoOption.BuildTime
  )
  .enablePlugins(BuildInfoPlugin, AshScriptPlugin)

releaseProcess :=
  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    setReleaseVersion,
    releaseStepCommandAndRemaining("docker:publish"),
    commitReleaseVersion,
    tagRelease,
    inquireVersions,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
