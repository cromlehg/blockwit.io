name := """blockwit.io"""
organization := "com.blockwit"

PlayKeys.playDefaultPort := 9000

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

scalaVersion := "2.12.3"

libraryDependencies += guice

libraryDependencies += "org.web3j" % "core" % "3.3.1"

libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "3.1"

libraryDependencies += "org.webjars" % "bootstrap" % "4.1.0"

libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT"

libraryDependencies += "org.jsoup" % "jsoup" % "1.11.2"
libraryDependencies += "com.atlassian.commonmark" % "commonmark" % "0.11.0"
libraryDependencies += "com.atlassian.commonmark" % "commonmark" % "0.11.0"
libraryDependencies += "com.atlassian.commonmark" % "commonmark-ext-gfm-tables" % "0.11.0"

libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "6.0.5"

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

libraryDependencies += "com.sendgrid" % "java-http-client" % "4.2.0"
libraryDependencies += "com.sendgrid" % "sendgrid-java" % "4.1.2"

libraryDependencies += "org.ocpsoft.prettytime" % "prettytime" % "4.0.1.Final"

libraryDependencies += "org.webjars" % "font-awesome" % "5.0.8"

