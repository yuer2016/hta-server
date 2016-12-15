import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

lazy val project = Project(
  id = "hta-server",
  base = file("."),
  settings = Defaults.coreDefaultSettings ++ Seq(
    name := "hta-server",
    version := "1.0.0",
    scalaVersion := "2.11.8",
    resolvers ++= Seq(
      "chrisdinn" at "http://chrisdinn.github.io/releases/",
      "SpinGo OSS" at "http://spingo-oss.s3.amazonaws.com/repositories/releases",
      "spray repo" at "http://repo.spray.io"
    ),
    libraryDependencies ++= {
      val akkaVersion = "2.4.9"
      val jacksonVersion = "2.8.3"
      Seq(
        "org.scalatest" %% "scalatest" % "2.2.1" % "test",
        "com.typesafe.akka" %% "akka-actor" % akkaVersion,
        "com.typesafe.akka" %% "akka-remote" % akkaVersion,
        "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
        "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
        "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
        "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
        "com.typesafe.slick" %% "slick" % "3.2.0-M1",
        "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0-M1",
        "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonVersion,
        "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
        "mysql" % "mysql-connector-java" % "5.1.38",
        "com.aliyun.oss" % "aliyun-sdk-oss" % "2.0.7",
        "org.apache.httpcomponents" % "httpclient" % "4.4",
        "org.jdom" % "jdom" % "1.1",
        "io.spray" %% "spray-json" % "1.3.2",
        "com.typesafe.play" %% "play-json" % "2.4.4",
        "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.0"
      )
    },
    test in assembly := {},
    mainClass in assembly := Some("com.amitek.TermSimulator"),
    assemblyOutputPath in assembly := file(s"./release/${name.value}-${version.value}.jar")
  )
)


    