package joky.spark.flow.spark.busi

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.task.Task
import joky.spark.flow.{AppPushFunctionNode, FlowNode, MailFunctionNode}
import joky.spark.flow.spark.{SparkBaseBusi, SparkBusiConfig}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/19 14:43
  * @Description:
  */
case class SparkAppPushFunctionBusi(flowId: Long,
                                    flowNode: FlowNode,
                                    appPushFunctionNode: AppPushFunctionNode)
    extends SparkBaseBusi(flowId, flowNode, appPushFunctionNode) {

    override def getTask(startTime: Timestamp,
                         timeWindow: Int,
                         timeWindowUnit: TimeUnit,
                         sparkBusiConfig: SparkBusiConfig): Task = {
        new Task {
            override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
                val ds = setCurrentFlowNodeUser(startTime, father, spark)
                ds.foreach(f => println(s"app push to userId=${f.user_id} deviceId=${f.device_id}"))
                ds.toDF()
            }
        }
    }
}
