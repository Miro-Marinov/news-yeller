name := "news-yeller"

version := "0.1"

scalaVersion := "2.12.5"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.bintrayRepo("hseeberger", "maven")
resolvers += Resolver.jcenterRepo

val akkaHttpVersion = "10.0.10"
val akkaVersion = "2.5.12"
val akkaPersistenceVersion = "2.5.12"
val slickVersion  = "3.2.3"
libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.9",
  "com.google.inject" % "guice" % "4.1.0",
  "org.json4s" %% "json4s-native" % "3.5.3",

  "com.typesafe.akka" %% "akka-persistence" % akkaPersistenceVersion,
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.8.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.20",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",
  "com.lightbend" %% "kafka-streams-scala" % "0.2.1",

  "de.heikoseeberger" %% "akka-http-json4s" % "1.18.0",

  "org.postgresql" % "postgresql" % "42.2.2",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "org.slf4j" % "slf4j-log4j12" % "1.7.25"
)