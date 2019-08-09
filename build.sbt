name := "ads-txt-crawler"

version := "0.1"

scalaVersion := "2.13.0"

val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.9"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.4",

  "com.typesafe.play" %% "play-json" % "2.7.4",

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

  "com.google.inject" % "guice" % "4.2.2",

  "commons-validator" % "commons-validator" % "1.6",

  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

herokuAppName in Compile := "ads-txt-crawler"

enablePlugins(JavaServerAppPackaging)
