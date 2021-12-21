name := "dlcrypto_core"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.53",
  "joda-time" % "joda-time" % "2.2",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.7",
  "com.softwaremill.macwire" %% "macros" % "2.5.0" % "provided",
  "com.softwaremill.macwire" %% "util" % "2.5.0",
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  // For ScalaPB 0.11.x (json4s 0.3.x):
  "com.thesamet.scalapb" %% "scalapb-json4s" % "0.11.1"
  )

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
// retrieveManaged := true
