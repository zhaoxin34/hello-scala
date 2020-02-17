package joky.spark.flow.spark

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.task.Task
import joky.spark.flow.exception.FlowNodeRunException
import joky.spark.flow.{FlowNode, FlowNodeBusi, FlowNodeUser}
import org.apache.spark.sql.functions.typedLit
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

import scala.util.{Failure, Success, Try}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/16 21:46
  * @Description: spark运行业务的基类
  */
abstract class SparkBaseBusi[T >: FlowNodeBusi](flowId: Long,
                                                flowNode: FlowNode,
                                                flowNodeBusi: T) {

    def getTask(startTime: Timestamp,
                timeWindow: Int,
                timeWindowUnit: TimeUnit): Task

    final def runTask(startTime: Timestamp,
                timeWindow: Int,
                timeWindowUnit: TimeUnit,
                fatherFlowNodeUserDs: Dataset[FlowNodeUser],
                spark: SparkSession): Try[Dataset[FlowNodeUser]] = {

        val fatherDsTry = Option(fatherFlowNodeUserDs).map(_.toDF()).map(Success(_)).getOrElse(Success(null))
        import spark.implicits._

        getTask(startTime, timeWindow, timeWindowUnit).run(fatherDsTry, spark) match {
            case Success(x: DataFrame) => Success(x.as[FlowNodeUser])
            case Failure(e) => Failure(e)
            case _ => Failure(FlowNodeRunException(s"未知节点返回值错误", flowNode))
        }
    }

    protected def setCurrentFlowNodeUser(startTime: Timestamp,
                                         dataFrame: DataFrame,
                                         spark: SparkSession): Dataset[FlowNodeUser] = {
        import spark.implicits._
        dataFrame
            .withColumn("flow_id", typedLit[Long](flowId))
            .withColumn("flow_node_id", typedLit[Int](flowNode.nodeId))
            .withColumn("branch_index", typedLit[Int](0))
            .withColumn("stat_time", typedLit[Timestamp](startTime))
            .withColumn("timeout_minute", typedLit[Int](0))
            .withColumn("flow_node_name", typedLit[String](this.getClass.getCanonicalName))
            .as[FlowNodeUser]
    }

}
//
//abstract class FlowNodeUserTask extends Task {
//    def executeFlowNodeUserTask(father: Dataset[FlowNodeUser], spark: SparkSession): Dataset[FlowNodeUser]
//
//    final override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
//        import spark.implicits._
//        executeFlowNodeUserTask(father.as[FlowNodeUser], spark).toDF()
//    }
//}
