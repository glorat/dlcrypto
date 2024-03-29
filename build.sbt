name := "dlcrypto-root"

organization := "net.glorat"

lazy val scala213 = "2.13.7"
lazy val scala212 = "2.12.15"
lazy val supportedScalaVersions = List(scala213, scala212)

ThisBuild / organization := "net.glorat"
ThisBuild / version      := "0.2.3"
ThisBuild / scalaVersion := scala213

resolvers += Classpaths.typesafeReleases

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

updateOptions := updateOptions.value.withCachedResolution(true)

lazy val commonSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  licenses := Seq("GNU LESSER GENERAL PUBLIC LICENSE" -> url("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt")),
  crossScalaVersions := supportedScalaVersions,
  PB.protocVersion := "3.21.7"
)

lazy val dlcrypto_core = project.settings(commonSettings, fork  := true)

lazy val dlcrypto_ecdsa = project.settings(commonSettings ,fork  := true).dependsOn(dlcrypto_core % "compile->compile;test->test", dlcrypto_encode)

lazy val dlcrypto_encode = project.settings(commonSettings)dependsOn(dlcrypto_core % "compile->compile;test->test;protobuf->protobuf")

lazy val dlcrypto_mock = project.settings(commonSettings).dependsOn(dlcrypto_core % "compile->compile;test->test", dlcrypto_encode)

lazy val root = (project in file("."))
  .aggregate(dlcrypto_core, dlcrypto_ecdsa, dlcrypto_encode, dlcrypto_mock)
  .settings(commonSettings ++ Seq(packagedArtifacts := Map.empty))

    .settings(
      crossScalaVersions := Nil,
      publish / skip := true
    )

ThisBuild / homepage := Some (url("https://github.com/glorat/dlcrypto"))

ThisBuild / scmInfo := Some (
  ScmInfo(
    url("https://github.com/glorat/dlcrypto"),
    "scm:git@github.com:glorat/dlcrypto.git"
  )
)

ThisBuild / developers := List (
  Developer(
    id = "glorat",
    name = "Kevin Tam",
    email = "kevin@glorat.net",
    url = url("https://github.com/glorat")
  )
)

ThisBuild / publishTo := sonatypePublishTo.value

// Useful to uncomment for snapshots or bad publishes
ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true)
ThisBuild / publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)
