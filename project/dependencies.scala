import sbt.Keys.crossPaths
import sbt._

object Dep {
    val versionSpark = "2.4.3"
    val versionHbase = "1.2.0"
    val versionLog4jApiScala = "11.0"
    val versionJackson = "2.6.5"
    val versionScalaTest = "3.0.5"
    val versionNscalaTime = "2.20.0"
    val versionAkka = "2.5.19"

    val log4jScala = "org.apache.logging.log4j" %% "log4j-api-scala" % versionLog4jApiScala
    val log4jApi = "org.apache.logging.log4j" % "log4j-api" % "2.11.1"
    val log4jCore = "org.apache.logging.log4j" % "log4j-core" % "2.11.1"
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

    val nscalaTime = "com.github.nscala-time" %% "nscala-time" % versionNscalaTime

    val apacheMath = "org.apache.commons" % "commons-math3" % "3.6"
    
    val erSlf4j = ExclusionRule("org.slf4j", "slf4j-log4j12")
    val erSlf4jApi = ExclusionRule("org.slf4j", "slf4j-api")
//    val erSlf4j = ExclusionRule("org.slf4j", "*")
    val erServlet = ExclusionRule("javax.servlet", "servlet-api")
    val erJsp = ExclusionRule("javax.servlet.jsp", "jsp-api")
    val erJetty = ExclusionRule("org.mortbay.jetty", "servlet-api-2.5")
    val erJerseyServer = ExclusionRule("com.sun.jersey", "jersey-server")
    
    val sparkCore = "org.apache.spark" %% "spark-core" % versionSpark
    val sparkSql = "org.apache.spark" %% "spark-sql" % versionSpark
    val sparkSqlKafka010 = "org.apache.spark" %% "spark-sql-kafka-0-10" % versionSpark
    val sparkMllib = "org.apache.spark" %% "spark-mllib" % versionSpark
    val sparkStreaming = "org.apache.spark" %% "spark-streaming" % versionSpark
    val sparkHive = "org.apache.spark" %% "spark-hive" % versionSpark
    val sparkStreamingKafka08 = "org.apache.spark" %% "spark-streaming-kafka-0-10" % versionSpark
    //        val sparkStreamKafka = "org.apache.spark" %% "spark-streaming-kafka" % "1.6.3"
    val scopt = "com.github.scopt" %% "scopt" % "3.7.0"
    val kafka = "org.apache.kafka" %% "kafka" % "1.1.1"
    val kafkaClient = "org.apache.kafka" % "kafka-clients" % "0.10.2.1"
    val hbaseClient = "org.apache.hbase" % "hbase-client" % versionHbase excludeAll(erSlf4j, erServlet, erJsp, erJetty, erJerseyServer)
    val hbaseCommon = "org.apache.hbase" % "hbase-common" % versionHbase excludeAll(erSlf4j, erServlet, erJsp, erJetty, erJerseyServer)
    val hbaseServer = "org.apache.hbase" % "hbase-server" % versionHbase excludeAll(erSlf4j, erServlet, erJsp, erJetty, erJerseyServer)
//    val shcCore = "com.hortonworks" % "shc-core" % "1.1.1-2.1-s_2.11"
    val sparklintSpark220 = "com.groupon.sparklint" %% "sparklint-spark220" % "1.0.12"
    val sparkEs = "org.elasticsearch" %% "elasticsearch-spark-20" % "7.3.0"

    val depsAkka = Seq (
        "com.typesafe.akka" %% "akka-actor" % versionAkka,
        "com.typesafe.akka" %% "akka-cluster" % versionAkka,
        "com.typesafe.akka" %% "akka-testkit" % versionAkka % Test
    )

    val depsScalatest = Seq(
        "org.scalatest" % "scalatest_2.11" % versionScalaTest % Test,
        "junit" % "junit" % "4.12" % Test,
        "com.novocode" % "junit-interface" % "0.11" % Test
    )
    val depsJackson = Seq(
        "com.fasterxml.jackson.core" % "jackson-core" % versionJackson,
        "com.fasterxml.jackson.core" % "jackson-annotations" % versionJackson,
        "com.fasterxml.jackson.core" % "jackson-databind" % versionJackson,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % versionJackson,
        "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % versionJackson
    )
    
    val depsLog4j = Seq(
        log4jApi,
        log4jCore,
        log4jScala
    )
    val depsSpark = Seq(
        sparkCore,
        sparkSql,
        sparkSqlKafka010,
        sparkMllib,
        sparkStreaming,
        sparkHive,
        sparkStreamingKafka08,
        sparkEs
    )

    val depsSparkProvided = Seq(
        sparkCore % Provided,
        sparkSql % Provided,
        sparkSqlKafka010 % Provided,
        sparkMllib % Provided,
        sparkStreaming % Provided,
        sparkHive % Provided,
        sparkStreamingKafka08 % Provided
    )


    val depBigData = Seq(
        scopt,
        kafka,
        kafkaClient,
        hbaseClient,
        hbaseCommon,
        hbaseServer,
        logback
//        shcCore
    )

    val depsOverrideSpark = Seq(
        "com.fasterxml.jackson.core" % "jackson-core" % "2.6.5",
        "com.fasterxml.jackson.core" % "jackson-annotations" % "2.6.5",
        "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
    )
}
