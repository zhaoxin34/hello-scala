//package joky.spark.count.engine.impl.plan
//
//import joky.spark.count.engine.Plan
//import joky.spark.count.engine.impl.command.AggFunction
//import org.apache.spark.sql.{DataFrame, SparkSession}
//
//import scala.collection.mutable.ArrayBuffer
//
//abstract class MetricsPlan extends Plan {
//    var dataFrame: Option[DataFrame] = None
//    var sparkSession: Option[SparkSession] = None
//    val metricsList: ArrayBuffer[AggFunction] = new ArrayBuffer[AggFunction]()
//
//    def fromTable(table: String, sparkSession: SparkSession): MetricsPlan = {
//        dataFrame = Option(sparkSession.table(table))
//        this.sparkSession = Option(sparkSession)
//        this
//    }
//
//    def metrics(aggFunction: AggFunction): MetricsPlan = {
//        metricsList += aggFunction
//        this
//    }
//
////    override def run(): DataFrame = {
////
////    }
//}

