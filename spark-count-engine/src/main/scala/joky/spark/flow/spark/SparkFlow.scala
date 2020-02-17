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
                    ) extends Flow(flowId, flowNodes, flowRunConfig)  with Logging {

    logger.info(s"create flow, flowId=$flowId")
    spark.sparkContext.setLogLevel("warn")

    val flowContext: FlowContext = SparkFlowContext(flowId, spark, sparkBusiConfig)

    override def afterRunOnce(): Unit = {
        flowContext.asInstanceOf[SparkFlowContext].clear()
    }

    override def beforeRunOnce(): Unit = {

    }
}
