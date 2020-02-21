package joky.spark.flow.spark

import joky.spark.flow._
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.sql.SparkSession

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/14 13:39
  * @Description:
  */
case class SparkFlow(flowId: Long,
                     flowNodes: Seq[FlowNode],
                     flowRunConfig: FlowRunConfig,
                     spark: SparkSession,
                     sparkBusiConfig: SparkBusiConfig
                    ) extends Flow(flowId, flowNodes, flowRunConfig) with Logging {

    logger.info(s"create flow, flowId=$flowId")
    spark.sparkContext.setLogLevel("warn")

    val sparkFlowContext = SparkFlowContext(flowId, spark, sparkBusiConfig)
    val flowContext: FlowContext = sparkFlowContext

    import spark.implicits._
    import org.apache.spark.sql.functions._

    private def showOnceResult(): Unit = {
        sparkFlowContext.getAllFlowNodeUser()
            .groupBy("node_id", "flow_node_name")
            .agg(count($"node_id"))
            .orderBy("node_id")
            .show(1000)
    }

    override def afterRunOnce(): Unit = {
        showOnceResult()
        sparkFlowContext.getAllFlowNodeUser().write
            .option("mode", "append")
            .option("header", "true")
            .format("csv")
            .save("spark_data/table/flow_node_user/flow_node_user2.csv")
        sparkFlowContext.clear()
    }

    override def beforeRunOnce(): Unit = {

    }
}
