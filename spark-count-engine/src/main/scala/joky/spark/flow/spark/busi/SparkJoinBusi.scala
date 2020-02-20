package joky.spark.flow.spark.busi

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.task.Task
import joky.spark.flow.{FlowNode, JoinNode, MailFunctionNode}
import joky.spark.flow.spark.{SparkBaseBusi, SparkBusiConfig}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/19 14:48
  * @Description:
  */
case class SparkJoinBusi (flowId: Long,
                     flowNode: FlowNode,
                     joinNode: JoinNode)
    extends SparkBaseBusi(flowId, flowNode, joinNode) {

    override def getTask(startTime: Timestamp,
                         timeWindow: Int,
                         timeWindowUnit: TimeUnit,
                         sparkBusiConfig: SparkBusiConfig): Task = {
        new Task {
            override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
                setCurrentFlowNodeUser(startTime, father, spark).toDF()
            }
        }
    }
}
