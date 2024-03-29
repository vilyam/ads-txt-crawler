import sbt.Keys.{mainClass, packageBin}

lazy val commonSettings = Seq(
  name := "ads-txt-crawler",
  version := "0.1",
  organization := "com.viliamov",
  scalaVersion := "2.13.0",
  test in assembly := {}
)

lazy val app = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    skip in publish := true
  )
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(AssemblyPlugin)

lazy val cosmetic = project
  .settings(
    name := "shaded-something",
    packageBin in Compile := (assembly in(app, Compile)).value
  )

val akkaVersion = "2.5.23"
val akkaHttpVersion = "10.1.9"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0",

  "com.typesafe" % "config" % "1.3.4",

  "com.typesafe.play" %% "play-json" % "2.7.4",

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

  "redis.clients" % "jedis" % "3.1.0",

  "com.google.inject" % "guice" % "4.2.2",

  "commons-validator" % "commons-validator" % "1.6",

  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",

  "org.scalatest" %% "scalatest" % "3.0.8" % "test"
)

herokuAppName in Compile := "ads-txt-crawler"


