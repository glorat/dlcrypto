name := "dlcrypto_core"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.53",
  "joda-time" % "joda-time" % "2.2",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.softwaremill.macwire" %% "macros" % "2.3.2" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.3.2",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.2"
)

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
// retrieveManaged := true
