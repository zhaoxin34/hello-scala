package joky.producer.test

import joky.producer.PageConfig
import joky.producer.site.PageTree

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/30 22:32
  * @Description:
  */
object TestPageCreate extends App {

    val siteStringArr =
        """
          |http://spark.apache.org/docs/latest/, Overview - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/index.html, Overview - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/quick-start.html, Quick Start - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/rdd-programming-guide.html, RDD Programming Guide - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/sql-programming-guide.html, Spark SQL and DataFrames - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/structured-streaming-programming-guide.html, Structured Streaming Programming Guide - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/streaming-programming-guide.html, Spark Streaming - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/ml-guide.html, MLlib: Main Guide - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/graphx-programming-guide.html, GraphX - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/sparkr.html, SparkR (R on Spark) - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/api/scala/index.html, Spark 2.4.0 ScalaDoc
          |http://spark.apache.org/docs/latest/api/java/index.html, Spark 2.4.0 JavaDoc
          |http://spark.apache.org/docs/latest/api/python/index.html, Welcome to Spark Python API Docs! &amp;#8212; PySpark 2.4.0 documentation
          |http://spark.apache.org/docs/latest/api/R/index.html, Documentation of the SparkR package
          |http://spark.apache.org/docs/latest/api/sql/index.html, Spark SQL, Built-in Functions
          |http://spark.apache.org/docs/latest/cluster-overview.html, Cluster Mode Overview - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/submitting-applications.html, Submitting Applications - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/spark-standalone.html, Spark Standalone Mode - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/running-on-mesos.html, Running Spark on Mesos - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/running-on-yarn.html, Running Spark on YARN - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/running-on-kubernetes.html, Running Spark on Kubernetes - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/api.html, Spark API Documentation - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/configuration.html, Configuration - Spark 2.4.0 Documentation
          |http://spark.apache.org/docs/latest/monitoring.html, Monitoring and Instrumentation - Spark 2.4.0 Documentation
        """.stripMargin.split("\n")

    val pageList: Seq[PageConfig] = siteStringArr.map(str => str.split(",").map(_.trim)).filter(_.size == 2).map(arr => PageConfig(arr(0), arr(1)))

    val tree = PageTree.createPageTree(pageList)
    tree.printTree()
}
