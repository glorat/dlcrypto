name := "dlcrypto_core"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.53",
  "joda-time" % "joda-time" % "2.2",
  "org.scalatest" %% "scalatest" % "3.2.10" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.softwaremill.macwire" %% "macros" % "2.5.0" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.5.0",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  // For ScalaPB 0.11.x (json4s 0.3.x):
  "com.thesamet.scalapb" %% "scalapb-json4s" % "0.12.0"
)
Compile/PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value
)
// retrieveManaged := true
