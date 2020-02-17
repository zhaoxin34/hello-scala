package joky.spark.flow

import java.util
import java.util.Date

import joky.spark.FlowStarter.{flow, logger}
import joky.spark.flow.exception.FlowRunException
import org.apache.logging.log4j.scala.Logging

import scala.collection.JavaConversions._
import scala.concurrent.duration.TimeUnit
import scala.util.parsing.combinator.Parsers
import scala.util.{Failure, Success, Try}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/11 20:29
  * @Description:
  */
abstract class Flow(flowId: Long, flowNodes: Seq[FlowNode], flowRunConfig: FlowRunConfig) extends Logging {
    val flowContext: FlowContext

    def runOnce(startTime: Date, timeWindow: Int, timeWindowUnit: TimeUnit): Try[FlowResult] = {
        beforeRunOnce()
        try {
            val runningQueue = new util.LinkedList[FlowNode]()
            val waitingList = new util.LinkedList[FlowNode](flowNodes)

            // 是否存在某个节点，它不是开始节点，并且他的父节点不存在
            val invalidNodes: Seq[FlowNode] = waitingList.filter(
                node => node.joinType != FlowNodeJoinType.ENTRY && !waitingList.map(_.nodeId).containsAll(node.fatherIds))

            if (invalidNodes.nonEmpty) {
                return Failure(new FlowRunException(flowId, flowNodes, s"$invalidNodes 不是root节点并且它的父节点不存在这个流程中"))
            }

            while (waitingList.size() > 0) {
                val node = waitingList.pollFirst()
                // 如果是进入节点，运行节点，并将其子节点加入running，剔除出waiting
                if (node.joinType == FlowNodeJoinType.ENTRY) {
                    runningQueue.add(node)
                }
                // 父已经运行中了
                else if (runningQueue.map(_.nodeId).containsAll(node.fatherIds)) {
                    runningQueue.add(node)
                }
                else {
                    waitingList.addLast(node)
                }
            }

            logger.info(s"running queue ${runningQueue.map(node => node.name + ":" + node.nodeId).reduce(_ + "," + _)}")

            val flowNodeResults = new util.LinkedList[FlowNodeResult]()
            while(runningQueue.nonEmpty) {
                val flowNodeResult = runningQueue.pollFirst()
                    .run(startTime, flowRunConfig.timeWindow, flowRunConfig.timeWindowUnit, flowContext)
                if (flowNodeResult.isFailure)
                    return Failure(flowNodeResult.failed.get)
                flowNodeResults.add(flowNodeResult.get)
            }
            Success(FlowResult(flowId, flowNodeResults))
        }
        catch {
            case x: Exception => Failure(x)
        }
        finally {
            afterRunOnce()
        }
    }

    def afterRunOnce(): Unit

    def beforeRunOnce(): Unit

    def start(): Unit = {
        var runTime = 0
        while (runTime < flowRunConfig.runTimes || flowRunConfig.runTimes == -1) {
            val now = new Date()
            logger.info(s"start $runTime $now")

            runOnce(now, flowRunConfig.timeWindow, flowRunConfig.timeWindowUnit) match {
                case scala.util.Success(value) => logger.info(value)
                case scala.util.Failure(exception) =>
                    logger.error(s"执行发生错误", exception)
            }

            Thread.sleep(flowRunConfig.timeWindowUnit.toMillis(flowRunConfig.timeWindow))
            runTime = runTime + 1
        }
    }
}
