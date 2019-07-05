package joky.event.creator.test

import joky.event.creator.component.{Page, PageTree}
import joky.event.creator.creator.SiteCreator
import org.scalatest.FlatSpec

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/10 15:26
  * @Description:
  */
class TestPageCreator extends FlatSpec {

    "PageTree" should "createPageTree [value,subTree,pagelist,isRoot] right" taggedAs test.eventCreator in {
        val tree = PageTree.createPageTree(Seq(Page("http://www.aaa.com/a/b.html", "b page")))
        assert( tree.subTree(0).value == "http://www.aaa.com" )
        assert( tree.subTree(0).subTree(0).value === "a" )
        assert( tree.subTree(0).subTree(0).pageList(0).url === "http://www.aaa.com/a/b.html" )
        assert(tree.isRoot)
        assert(tree.subTree(0).subTree(0).father.get.father.get.isRoot)
    }

    it should "read from string noEmpty" taggedAs test.eventCreator in {
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

        val pageList: Seq[Page] = siteStringArr.map(str => str.split(",").map(_.trim)).filter(_.length == 2).map(arr => Page(arr(0), arr(1)))

        val tree = PageTree.createPageTree(pageList)
//                tree.printTree()
        assert(tree.subTree(0).subTree.nonEmpty)
    }

    it should "read from file" taggedAs test.eventCreator in {
        assert(SiteCreator.createSiteList().nonEmpty)
    }
}
