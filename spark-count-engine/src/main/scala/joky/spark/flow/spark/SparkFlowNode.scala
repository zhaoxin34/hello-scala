package joky.spark.flow.spark

import java.sql.Timestamp
import java.util.Date

import joky.spark.flow._
import joky.spark.flow.exception.FlowNodeRunException
import joky.spark.flow.spark.busi.{SparkMailFunctionBusi, SparkUserEntryBusi, SparkWaitTimerBusi}
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.sql.Dataset

import scala.concurrent.duration.TimeUnit
import scala.util.{Failure, Success, Try}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/14 16:06
  * @Description:
  */
case class SparkFlowNode(override val nodeId: Int,
                         override val name: String,
                         override val flowNodeBusiNode: FlowNodeBusi,
                         override val fatherIds: Seq[Int] = Seq(),
                         override val childrenIds: Seq[Int] = Seq(),
                         override val branchIndex: Int = 0)
    extends FlowNode(nodeId, name, flowNodeBusiNode, fatherIds, childrenIds, branchIndex) with Logging {

    override def run(startTime: Date,
                     timeWindow: Int,
                     timeWindowUnit: TimeUnit,
                     flowContext: FlowContext): Try[FlowNodeResult] = {
        logger.info(s"$name is running, nodeId=$nodeId")

        val sparkFlowContext = flowContext.asInstanceOf[SparkFlowContext]

        // 根据业务类型获得相应的task
        val sparkBusi = flowNodeBusiNode match {
            case userEntryNode: UserEntryNode => SparkUserEntryBusi(
                sparkFlowContext.flowId,
                this,
                userEntryNode,
                sparkFlowContext.getFlowNodeUserHistoryByNodeId(nodeId))

            case mailFunctionNode: MailFunctionNode => SparkMailFunctionBusi(
                sparkFlowContext.flowId,
                this,
                mailFunctionNode)

            case waitTimerNode: WaitTimerNode => SparkWaitTimerBusi(sparkFlowContext.flowId, this, waitTimerNode)
            case _ =>
                throw FlowNodeRunException(s"未知节点业务类型, flowNodeBusi=$flowNodeBusiNode", this)
        }

        // 获得父节点运行的ds
        val fatherDataSet: Dataset[FlowNodeUser] = sparkFlowContext.getFatherFlowNodeUser(fatherIds, branchIndex).orNull

        // 运行当前节点
        val taskResult: Try[Dataset[FlowNodeUser]] = sparkBusi.runTask(new Timestamp(startTime.getTime),
            timeWindow,
            timeWindowUnit,
            fatherDataSet,
            sparkFlowContext.spark)

        taskResult match {
            case Success(ds) =>
                sparkFlowContext.reserveResult(nodeId, ds)
                Success(FlowNodeResult())
            case scala.util.Failure(e) => Failure(e)
        }
    }
}
