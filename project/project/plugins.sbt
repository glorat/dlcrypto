resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.5")

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.11")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.2"

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.3.12")
addSbtPlugin("org.scalatra.sbt" % "sbt-scalatra" % "1.0.1")
