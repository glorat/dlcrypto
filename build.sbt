name := "dlcrypto-root"

organization := "net.glorat"

lazy val scala212 = "2.12.8"
lazy val scala211 = "2.11.12"
lazy val supportedScalaVersions = List(scala212, scala211)

ThisBuild / organization := "net.glorat"
ThisBuild / version      := "0.2.0"
ThisBuild / scalaVersion := scala212

resolvers += Classpaths.typesafeReleases

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

updateOptions := updateOptions.value.withCachedResolution(true)

lazy val commonSettings = Seq(
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  licenses := Seq("GNU LESSER GENERAL PUBLIC LICENSE" -> url("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt")),
  crossScalaVersions := supportedScalaVersions
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

publishTo in ThisBuild := sonatypePublishTo.value

// Useful to uncomment for snapshots or bad publishes
publishConfiguration in ThisBuild := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration in ThisBuild := publishLocalConfiguration.value.withOverwrite(true)

>>>>>>> github/master
