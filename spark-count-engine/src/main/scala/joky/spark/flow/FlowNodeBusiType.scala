package joky.spark.flow

import joky.spark.de.entity.{Filter, Table}
import joky.spark.de.task._

import scala.concurrent.duration.TimeUnit

/**
  * 分支信息
  * @param branchName
  * @param inCount
  */
case class BranchInfo(branchName: String, inCount: Int)

/**
  * 流程节点基类
  */
trait FlowNodeBusi

/**
  * 过程节点
  */
trait FunctionNode extends FlowNodeBusi

/**
  * 等待时间节点基类
  */
trait TimerNode extends FlowNodeBusi

/**
  * 切分性节点基类
  */
trait SplitNode extends FlowNodeBusi {
    def getBranchInfo(): Seq[BranchInfo]
}

/**
  * 入口型节点基类
  */
trait EntryNode extends FlowNodeBusi

/**
  * 等待时间节点
  * @param time
  * @param timeUnit
  */
case class WaitTimerNode(time: Int, timeUnit: TimeUnit) extends TimerNode

/**
  * 事件触发节点
  * @param eventName
  */
case class EventTriggerSplitNode(eventName: String) extends SplitNode {
    override def getBranchInfo(): Seq[BranchInfo] = Seq(
        BranchInfo("做过" + eventName, 0),
        BranchInfo("没做过" + eventName, 0)
    )
}

/**
  * 用户随机切分model
  * @param branchName
  * @param branchPercent
  */
case class UserSplit(branchName: String, branchPercent: Double)

/**
  * 随机用户分群节点
  * @param isAvg
  * @param branchList
  */
case class RandomUserSplitNode(isAvg: Boolean,  branchList: Seq[UserSplit]) extends SplitNode {
    override def getBranchInfo(): Seq[BranchInfo] = branchList.map(branch => BranchInfo(branch.branchName, 0))
}

/**
  * 用户过滤切分model
  * @param branchName
  * @param filter
  */
case class UserFilterSplit(branchName: String, filter: Filter)

/**
  * 用户过滤切分节点
  * @param branchList
  */
case class UserFilterSplitNode(branchList: Seq[UserFilterSplit]) extends SplitNode {
    override def getBranchInfo(): Seq[BranchInfo] =  branchList.map(branch => BranchInfo(branch.branchName, 0))
}

/**
  * 用户入口节点
  * @param table
  * @param filter
  */
case class UserEntryNode(table: Table, filter: Filter) extends EntryNode

/**
  * 事件入口节点
  */
case class EventEntryNode() extends EntryNode

/**
  * 邮件发送节点
  * @param title
  * @param content
  */
case class MailFunctionNode(title: String, content: String) extends FunctionNode

/**
  * app推送节点
  * @param content
  */
case class AppPushFunctionNode(content: String) extends FunctionNode

/**
  * join节点
  */
case class JoinNode() extends FlowNodeBusi
