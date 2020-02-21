package joky.spark.flow.spark.busi

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.task.{FilterTask, FromTableTask, Task}
import joky.spark.flow.spark.{SparkBaseBusi, SparkBusiConfig}
import joky.spark.flow.{FlowNode, FlowNodeUser, UserEntryNode}
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/14 22:55
  * @Description:
  */
case class SparkUserEntryBusi(flowId: Long,
                              flowNode: FlowNode,
                              userEntryNode: UserEntryNode,
                              currentFlowNodeUserHisotry: Dataset[FlowNodeUser])
    extends SparkBaseBusi(flowId, flowNode, userEntryNode) {

    override def getTask(startTime: Timestamp,
                         timeWindow: Int,
                         timeWindowUnit: TimeUnit,
                         sparkBusiConfig: SparkBusiConfig): Task = {

        FromTableTask(sparkBusiConfig.userTable) + FilterTask(userEntryNode.filter) + new Task {
            override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
                val df = father.join(currentFlowNodeUserHisotry,
                    father.col("user_id") === currentFlowNodeUserHisotry.col("user_id"), "left_outer")
                    .where("node_id is null")
                    .select(father.col("user_id"), father.col("mobile").as("device_id"))
                setCurrentFlowNodeUser(startTime, df, spark).toDF()
            }
        }

    }
}
