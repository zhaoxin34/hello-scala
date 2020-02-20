package joky.spark.flow.spark.busi

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.entity.SimpleFilter
import joky.spark.de.task.{FilterTask, FromTableTask, Task}
import joky.spark.flow.spark.{SparkBaseBusi, SparkBusiConfig}
import joky.spark.flow.{EventTriggerSplitNode, FlowNode, FlowNodeUser}
import org.apache.spark.sql.functions.typedLit
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.util.{Failure, Success}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/18 11:19
  * @Description:
  */
case class SparkEventTriggerSplitBusi(flowId: Long,
                                      flowNode: FlowNode,
                                      eventTriggerSplitNode: EventTriggerSplitNode,
                                      currentFlowNodeUserHisotry: Dataset[FlowNodeUser],
                                      fatherFlowNodeUserHisotry: Dataset[FlowNodeUser])
    extends SparkBaseBusi(flowId, flowNode, eventTriggerSplitNode) {

    override def getTask(startTime: Timestamp,
                         timeWindow: Int,
                         timeWindowUnit: TimeUnit,
                         sparkBusiConfig: SparkBusiConfig): Task = {
        //            AddValuedColumnTask(ValueColumn("branch_index", "", ColumnType.INTEGER, 0)) +
        new Task {
            override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
                import spark.implicits._

                // 事件df
                val eventDf = (
                    FromTableTask(sparkBusiConfig.eventTable) +
                        FilterTask(SimpleFilter(s"event_name='${eventTriggerSplitNode.eventName}'")))
                    .run(Success(null), spark) match {
                    case Success(value) => value
                    case Failure(e) => throw e
                }

                // 本次已完成事件的用户
                val eventDoneDf = eventDf.join(father, father.col("user_id") === eventDf.col("user_id"), "inner")
                    .select(eventDf("user_id"), father("device_id"))

                // 计算未完成事件的超时用户，为了减少计算量，只计算在两个窗口期内超时的用户，然后再根据当前节点的历史数据排重
                // 现在时间 > (上一步的时间 + 超时时间)  并且 现在时间  <= (上一步的时间 + 超时时间 + 2*窗口期)
                val now = new Timestamp(System.currentTimeMillis())
                val eventNotDoneDf = fatherFlowNodeUserHisotry
                    // 过滤已经超时的用户出来
                    .filter(col => {
                    val predictTimeoutTime = col.stat_time.getTime + TimeUnit.MINUTES.toMillis(col.timeout_minute)
                    now.getTime > predictTimeoutTime // && now.getTime <= predictTimeoutTime + 2 * timeWindowUnit.toMillis(timeWindow)
                })

                println(s"eventNotDoneDf = ${eventNotDoneDf.count()}")

                // 通过join方式排重已经入库的用户
                val eventNotDoneUniqueDf = eventNotDoneDf.join(
                    currentFlowNodeUserHisotry.filter("branch_index = 1").select($"user_id".as("his_user_id")),
                    $"user_id" === $"his_user_id", "left_outer")
                    .where("his_user_id is null")
                    .select(eventNotDoneDf("user_id"), eventNotDoneDf("device_id"))
                    .select("user_id", "device_id")

                println(s"eventNotDoneUniqueDf = ${eventNotDoneUniqueDf.count()}")
                val eventDoneFlowNodeUser = setCurrentFlowNodeUser(startTime, eventDoneDf, spark)
                    .withColumn("branch_index", typedLit[Int](0))
                val eventNotDoneFlowNodeUser = setCurrentFlowNodeUser(startTime, eventNotDoneUniqueDf, spark)
                    .withColumn("branch_index", typedLit[Int](1))

                eventDoneFlowNodeUser.select("user_id")

                eventDoneFlowNodeUser.union(eventNotDoneFlowNodeUser).toDF()
            }
        }
    }
}
