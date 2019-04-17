import Dep._
import sbt.Keys.{dependencyOverrides, resolvers}

name := "HelloScala"

lazy val root = project
    .in(file("."))
    .aggregate(
        core,
        producer,
        consumer,
        assemblyConsumerDepdencyJar
    )
    .settings(
        update / aggregate := false
    )

lazy val commonSettings = Seq(
    organization := "joky",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.11.12",
    artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
        artifact.name + "-" + module.revision + "." + artifact.extension
    },
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
        "central" at "http://repo1.maven.org/maven2/",
        "mvnrepository" at "https://mvnrepository.com/artifact"
    ),
    logLevel in compile := Level.Warn,
    libraryDependencies ++= (
        Dep.depsScalatest :+ Dep.nscalaTime :+ Dep.scopt),
    crossPaths := false,
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a")),
    updateOptions := updateOptions.value.withCachedResolution(true)
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

lazy val eventCreator = (project in file("event-creator"))
    .settings(
        commonSettings,
        name := "event-creator",
        libraryDependencies ++= Dep.depsLog4j
    )
    .dependsOn(core)

lazy val sparkCountEngine = (project in file("spark-count-engine"))
    .settings(
        commonSettings,
        name := "spark-count-engine",
        libraryDependencies ++= Dep.depsSpark,
        dependencyOverrides ++= depsOverrideSpark,
        excludeDependencies ++= Seq(Dep.erServlet, Dep.erJsp, Dep.erJetty, Dep.erJerseyServer)
    )
    .dependsOn(eventCreator)

lazy val producer = project
    .settings(
        commonSettings,
        name := "producer",
        libraryDependencies ++= (Dep.depsLog4j ++ Dep.depsAkka :+ kafkaClient)
    )
    .dependsOn(core)

lazy val consumer = project
    .settings(
        commonSettings,
        name := "consumer",
        resolvers ++= Seq(
            "hortonworks" at "http://repo.hortonworks.com/content/groups/public/"
        ),
        libraryDependencies ++= Dep.depsSparkProvided ++ Dep.depBigData,
        dependencyOverrides ++= depsOverrideSpark,
        excludeDependencies ++= Seq(Dep.erServlet, Dep.erJsp, Dep.erJetty, Dep.erJerseyServer)
    )
    .dependsOn(core)

lazy val sparkUdf = project
    .settings(
        commonSettings,
        name := "sparkUdf",
        resolvers ++= Seq(
            "hortonworks" at "http://repo.hortonworks.com/content/groups/public/"
        ),
        libraryDependencies ++= Dep.depsSparkProvided,
        dependencyOverrides ++= depsOverrideSpark,
        excludeDependencies ++= Seq(Dep.erServlet, Dep.erJsp, Dep.erJetty, Dep.erJerseyServer)
    )

lazy val assemblyConsumerDepdencyJar = (project in file("assembly/assembly-consumer-depdency-jar"))
    .dependsOn(consumer)
    .settings(
        name := "assemblyConsumerDepdencyJar",
        assemblyMergeStrategy in assembly := {
            case PathList("joky", xs@_*) => MergeStrategy.discard
            case PathList("org", "aopalliance", xs@_*) => MergeStrategy.last
            case PathList("javax", "inject", xs@_*) => MergeStrategy.last
            case PathList("javax", "servlet", xs@_*) => MergeStrategy.last
            case PathList("javax", "activation", xs@_*) => MergeStrategy.last
            case PathList("org", "apache", xs@_*) => MergeStrategy.last
            case PathList("org", "w3c", "dom", xs@_*) => MergeStrategy.last
            case PathList("com", "google", xs@_*) => MergeStrategy.last
            case PathList("com", "esotericsoftware", xs@_*) => MergeStrategy.last
            case PathList("com", "codahale", xs@_*) => MergeStrategy.last
            case PathList("com", "yammer", xs@_*) => MergeStrategy.last
            case "about.html" => MergeStrategy.rename
            case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
            case "META-INF/mailcap" => MergeStrategy.last
            case "META-INF/mimetypes.default" => MergeStrategy.last
            case "plugin.properties" => MergeStrategy.last
            case "log4j.properties" => MergeStrategy.last
            case "overview.html" => MergeStrategy.last // Added this for 2.1.0 I think
            case x =>
                val oldStrategy = (assemblyMergeStrategy in assembly).value
                oldStrategy(x)
        },
        assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
        assemblyJarName in assembly := "assembly-producer-depdency-jar.jar"
    )

