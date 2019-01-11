import Dep._
import sbt.Keys.{dependencyOverrides, resolvers}

name := "HelloScala"

lazy val root = project
    .in(file("."))
    .aggregate(
        core,
        producer,
        consumer
    )
    .settings(
        update / aggregate := false
    )

lazy val commonSettings = Seq(
    organization := "joky",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.12",
    scalacOptions ++= Seq(
        "-unchecked",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-language:implicitConversions",
        "-language:postfixOps",
        "-deprecation",
        "-encoding",
        "utf8"
    ),
    resolvers ++= Seq(
        //        "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
        "central" at "http://repo1.maven.org/maven2/",
        "mvnrepository" at "https://mvnrepository.com/artifact"
    ),
    logLevel in compile := Level.Warn,
    libraryDependencies ++= (
        Dep.depsScalatest :+ Dep.nscalaTime :+ Dep.scopt),
    crossPaths := false,
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a"))
    // mainClass in (Compile, packageBin) := Some("myproject.MyMain"),
    // publishTo := Some("name" at "url"),
    // fork a new JVM for 'run' and 'test:run'
    // fork := true,
    // fork a new JVM for 'test:run', but not 'run'
    // fork in Test := true,
    // add a JVM option to use when forking a JVM for 'run'
    // javaOptions += "-Xmx2G",
)

lazy val core = project
    .settings(
        commonSettings,
        name := "core",
        libraryDependencies ++= Dep.depsJackson
    )

lazy val producer = project
    .settings(
        commonSettings,
        name := "producer",
        libraryDependencies ++= (Dep.depsLog4j ++ Dep.depsAkka :+ kafkaClient )
    )
    .dependsOn(core)

lazy val consumer = project
    .settings(
        commonSettings,
        name := "consumer",
        resolvers ++= Seq(
            "hortonworks" at "http://repo.hortonworks.com/content/groups/public/"
        ),
        libraryDependencies ++= Dep.depsSpark,
        dependencyOverrides ++= depsOverrideSpark
    )
    .dependsOn(core)

