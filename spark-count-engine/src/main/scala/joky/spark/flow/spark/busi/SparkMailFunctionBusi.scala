package joky.spark.flow.spark.busi

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.task.Task
import joky.spark.flow.{FlowNode, FlowNodeUser, MailFunctionNode, UserEntryNode}
import joky.spark.flow.spark.{SparkBaseBusi, SparkFlowContext}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/16 21:45
  * @Description:
  */
case class SparkMailFunctionBusi(flowId: Long,
                                 flowNode: FlowNode,
                                 mailFunctionBusi: MailFunctionNode)
    extends SparkBaseBusi(flowId, flowNode, mailFunctionBusi) {

    override def getTask(startTime: Timestamp,
                         timeWindow: Int,
                         timeWindowUnit: TimeUnit): Task = {
        new Task {
            override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
                val ds = setCurrentFlowNodeUser(startTime, father, spark)
                ds.foreach(f => println(s"send mail to userId=${f.user_id} deviceId=${f.device_id}"))
                ds.toDF()
            }
        }
    }
}