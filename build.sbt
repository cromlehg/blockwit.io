name := """blockwit.io"""
organization := "com.blockwit"

PlayKeys.playDefaultPort := 9000

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

scalaVersion := "2.12.6"

// To prevent binary incompatible wranings! 
val akkaVersion = "2.5.11"
dependencyOverrides ++= Seq( 
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.google.guava" % "guava" % "22.0"
)

libraryDependencies ++= Seq(
  guice,
  "org.web3j" % "core" % "3.3.1",
  "com.github.t3hnar" %% "scala-bcrypt" % "3.1",
  "org.webjars" % "bootstrap" % "4.1.0",
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT",
  "org.jsoup" % "jsoup" % "1.11.2",
  "com.atlassian.commonmark" % "commonmark" % "0.11.0",
  "com.atlassian.commonmark" % "commonmark" % "0.11.0",
  "com.atlassian.commonmark" % "commonmark-ext-gfm-tables" % "0.11.0",
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.slick" %% "slick-codegen" % "3.2.3",
  "mysql" % "mysql-connector-java" % "6.0.5",
  "com.sendgrid" % "java-http-client" % "4.2.0",
  "com.sendgrid" % "sendgrid-java" % "4.1.2",
  "org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final",
  "org.webjars" % "font-awesome" % "5.0.8",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.3.0",
  "org.flywaydb" %% "flyway-play" % "5.0.0",
  "be.objectify" %% "deadbolt-scala" % "2.6.1",

  // Only for testing
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0-M1" % Test,
  "com.h2database" % "h2" % "1.4.192" % Test
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
javaOptions in Test += "-Dconfig.file=conf/application.test.conf"
