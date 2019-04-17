package joky.spark.count.engine

import joky.spark.count.engine.impl.command.Metrics
import org.apache.spark.sql.{DataFrame, SparkSession}

// a=segment[some where sql]
// ds = dataFrame[
// ]
//class SparkCountEngine(val sparkSession: SparkSession) {
//    def test(a: String): Unit = {
//        val scc = new SparkCountContext(sparkSession, sparkSession.table("event"))
//        val a  = SparkCountEngine.test
//    }
//
//}
//
//object Test {
//    def test(a: String): String = {
//        "abc"
//    }
//
//    val a: String => String = Test.test
//}
//private class SparkCountContext(val sparkSession: SparkSession, val dataFrame: DataFrame) {
//    import sparkSession.implicits._
//
//}
