name := "dlcrypto-root"

version := "0.1"

organization := "net.glorat"

scalaVersion in GlobalScope := "2.11.7"

resolvers += Classpaths.typesafeReleases

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

updateOptions := updateOptions.value.withCachedResolution(true)

lazy val commonSettings = Seq(
  organization := "net.glorat",
  version := "0.1.2",
  scalaVersion := "2.11.7",
  publishMavenStyle := true,
  pomIncludeRepository := { _ => false },
  licenses := Seq("GNU LESSER GENERAL PUBLIC LICENSE" -> url("https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt"))
)

lazy val dlcrypto_core = project.settings(commonSettings, fork  := true)

lazy val dlcrypto_ecdsa = project.settings(commonSettings ,fork  := true).dependsOn(dlcrypto_core % "compile->compile;test->test", dlcrypto_encode)

lazy val dlcrypto_encode = project.settings(commonSettings)dependsOn(dlcrypto_core % "compile->compile;test->test;protobuf->protobuf")

lazy val dlcrypto_mock = project.settings(commonSettings).dependsOn(dlcrypto_core % "compile->compile;test->test", dlcrypto_encode)

lazy val root = (project in file("."))
  .aggregate(dlcrypto_core, dlcrypto_ecdsa, dlcrypto_encode, dlcrypto_mock)
  .settings(commonSettings)

homepage := Some (url("https://github.com/glorat/dlcrypto"))

scmInfo := Some (
  ScmInfo(
    url("https://github.com/glorat/dlcrypto"),
    "scm:git@github.com:glorat/dlcrypto.git"
  )
)

developers := List (
  Developer(
    id = "glorat",
    name = "Kevin Tam",
    email = "kevin@glorat.net",
    url = url("https://github.com/glorat")
  )
)