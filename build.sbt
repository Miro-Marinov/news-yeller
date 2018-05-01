name := "news-yeller"

version := "0.1"

scalaVersion := "2.12.5"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

val akkaHttpVersion = "10.0.10"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.9",
  "com.google.inject" % "guice" % "4.1.0",
  "org.json4s" %% "json4s-native" % "3.5.3",

  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.20",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "com.lightbend" %% "kafka-streams-scala" % "0.2.1",

  "de.heikoseeberger" %% "akka-http-json4s" % "1.18.0"
)