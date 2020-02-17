package joky.spark.flow

import java.util.Date

import joky.spark.de.task.Task

import scala.concurrent.duration.TimeUnit
import scala.util.Try

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/11 20:27
  * @Description:
  */
abstract class FlowNode(val nodeId: Int,
                        val name: String,
                        val flowNodeBusiNode: FlowNodeBusi,
                        val fatherIds: Seq[Int] = Seq(),
                        val childrenIds: Seq[Int] = Seq(),
                        val branchIndex: Int = 0) {

    def joinType: FlowNodeJoinType.JoinType = (fatherIds, childrenIds) match {
        case (Seq(), Seq(_)) => FlowNodeJoinType.ENTRY
        case (Seq(_), Seq(_)) => FlowNodeJoinType.FUNCTION
        case (Seq(_), Seq(_, _*)) => FlowNodeJoinType.SPLIT
        case (Seq(_, _*), Seq(_)) => FlowNodeJoinType.JOIN
        case (Seq(_*), Seq()) => FlowNodeJoinType.JOIN
        case _ => FlowNodeJoinType.UNKNOW
    }

    def run(startTime: Date, timeWindow: Int, timeWindowUnit: TimeUnit, flowContext: FlowContext): Try[FlowNodeResult]

    override def toString = s"FlowNode(nodeId=$nodeId, name=$name, flowNodeBusiNode=$flowNodeBusiNode, fatherIds=$fatherIds, childrenIds=$childrenIds, branchIndex=$branchIndex, joinType=$joinType)"
}
