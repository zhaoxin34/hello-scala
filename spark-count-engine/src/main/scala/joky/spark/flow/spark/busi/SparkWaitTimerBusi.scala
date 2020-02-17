package joky.spark.flow.spark.busi

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.task.Task
import joky.spark.flow.spark.SparkBaseBusi
import joky.spark.flow.{FlowNode, WaitTimerNode}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/17 20:17
  * @Description:
  */
case class SparkWaitTimerBusi(flowId: Long,
                              flowNode: FlowNode,
                              waitTimerNode: WaitTimerNode) extends SparkBaseBusi(flowId, flowNode, waitTimerNode) {
    override def getTask(startTime: Timestamp, timeWindow: Int, timeWindowUnit: TimeUnit): Task = {
        new Task {
            override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
                import spark.implicits._
                setCurrentFlowNodeUser(startTime, father, spark)
                    .withColumn("timeout_minute", $"timeout_minute" + waitTimerNode.timeUnit.toMinutes(waitTimerNode.time).intValue())
            }
        }
    }
}
