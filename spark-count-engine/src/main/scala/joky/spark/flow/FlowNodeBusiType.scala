package joky.spark.flow

import joky.spark.de.entity.{Filter, Table}
import joky.spark.de.task._

import scala.concurrent.duration.TimeUnit

case class BranchInfo(branchName: String, inCount: Int)

trait FlowNodeBusiNode {
    def getTask(): Task
}

trait FunctionNode extends FlowNodeBusiNode
trait TimerNode extends FlowNodeBusiNode
trait SplitNode extends FlowNodeBusiNode {
    def getBranchInfo(): Seq[BranchInfo]
}
trait EntryNode extends FlowNodeBusiNode

case class WaitTimerNode(time: Int, timeUnit: TimeUnit) extends TimerNode {
    override def getTask(): Task = MapTask()
}

case class EventTriggerSplitNode(eventName: String) extends SplitNode {
    override def getBranchInfo(): Seq[BranchInfo] = Seq(
        BranchInfo("做过" + eventName, 0),
        BranchInfo("没做过" + eventName, 0)
    )

    override def getTask(): Task = MapTask()
}

//case class RandomUserSplitNode() extends SplitNode
//case class UserFilterSplitNode() extends SplitNode

case class UserEntryNode(table: Table, filter: Filter) extends EntryNode {
    override def getTask(): Task = SeqTask(
        FromTableTask(table),
        FilterTask(filter)
    )
}

case class EventEntryNode() extends EntryNode {
    override def getTask(): Task = MapTask()
}

case class MailFunctionNode(title: String, content: String) extends FunctionNode {
    override def getTask(): Task = MapTask()
}

case class AppPushFunctionNode(content: String) extends FunctionNode {
    override def getTask(): Task = MapTask()
}

case class JoinNode() extends FlowNodeBusiNode {
    override def getTask(): Task = MapTask()
}
