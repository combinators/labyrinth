//import play.sbt.PlayLayoutPlugin
//import play.twirl.sbt.SbtTwirl

val circeVersion = "0.11.1"

lazy val commonSettings = Seq(
  version := "1.0.0-SNAPSHOT",
  organization := "org.combinators",
  
  scalaVersion := "2.12.4",

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases"),
    "MQTT Repository" at "https://repo.eclipse.org/content/repositories/paho-releases/"
  ),

  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions"
  ),

  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),

  libraryDependencies ++= Seq(
    "org.combinators" %% "cls-scala" % "2.1.0+7-9e42ea3e",
    "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.2.0",
    "org.scalactic" %% "scalactic" % "3.0.1" % "test",
    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
    //"org.combinators" %% "cls-scala-ide" % "657119c7"
    //"org.combinators" %% "templating" % "1.0.0+3-bee373e9",
    //"org.combinators" %% "cls-scala-presentation-play-git" % "1.0.0-RC1+8-63d5cf0b",
    //guice
  ) ++
    Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)


)

lazy val root = Project(id = "labyrinth", base = file("."))
  .settings(commonSettings: _*)
//  .enablePlugins(SbtTwirl)
//  .enablePlugins(PlayScala)
//  .disablePlugins(PlayLayoutPlugin)
  .settings(moduleName := "labyrinth")

