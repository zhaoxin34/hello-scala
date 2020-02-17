package joky.spark.flow.spark

import joky.spark.FlowTester.spark
import joky.spark.flow.{FlowContext, FlowNodeUser}
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.collection.mutable

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/14 22:47
  * @Description:
  */
case class SparkFlowContext(flowId: Long, spark: SparkSession, sparkBusiConfig: SparkBusiConfig)
    extends FlowContext with Logging {

    import spark.implicits._

    logger.error(s"create a flow context, flowId=$flowId")

    private val flowNodeResultMap = new mutable.HashMap[Int, Dataset[FlowNodeUser]]()

    def getFlowNodeUserHistory(): Dataset[FlowNodeUser] = {
        spark.table(sparkBusiConfig.flowNodeUserTable.toString).as[FlowNodeUser].filter(f => f.flow_id == flowId)
    }

    def getFlowNodeUserHistoryByNodeId(nodeId: Int): Dataset[FlowNodeUser] = {
        getFlowNodeUserHistory().filter(_.flow_node_id == nodeId)
    }

    def reserveResult(flowNodeId: Int, value: Dataset[FlowNodeUser]): Unit = {
        value.show(10)
        flowNodeResultMap += (flowNodeId -> value.cache())
    }

    def getFatherFlowNodeUser(fatherIds: Seq[Int], branchIndex: Int): Option[Dataset[FlowNodeUser]] = {
        fatherIds
            .filter(flowNodeResultMap.contains)
            .map(nodeId => flowNodeResultMap(nodeId))
            .map(_.filter(_.branch_index == branchIndex))
            .reduceOption(_.union(_))
    }

    def clear(): Unit = {
        flowNodeResultMap.values.foreach(_.unpersist())
        flowNodeResultMap.clear()
        logger.info(s"flowId=$flowId, cleared")
    }
}
