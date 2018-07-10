name := "news-yeller"

version := "0.1"

scalaVersion := "2.12.5"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.bintrayRepo("hseeberger", "maven")
resolvers += Resolver.jcenterRepo


// Compatibility issues:
// https://doc.akka.io/docs/akka-http/10.0.11+20171201-1132/scala/http/compatibility-guidelines.html#akka-http-10-0-x-with-akka-2-5-x
// https://doc.akka.io/docs/akka-http/current/compatibility-guidelines.html#compatibility-with-akka
// https://github.com/romix/akka-kryo-serialization#how-to-use-this-library-in-your-project
val akkaHttpVersion = "10.1.3"
val akkaVersion = "2.5.13"
val slickVersion = "3.2.3"

libraryDependencies ++= Seq(
  "com.google.inject" % "guice" % "4.1.0",
  "org.json4s" %% "json4s-native" % "3.5.3",
  // Akka persistence
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "3.4.0",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,

  "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",

  "de.heikoseeberger" %% "akka-http-json4s" % "1.18.0",

  "org.postgresql" % "postgresql" % "42.2.2",
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "org.slf4j" % "slf4j-log4j12" % "1.7.25",

  "com.rometools" % "rome" % "1.8.1",

  "io.cryptocontrol" % "crypto-news-api" % "1.1.0",

  "org.scalaz" %% "scalaz-core" % "7.2.25"
)