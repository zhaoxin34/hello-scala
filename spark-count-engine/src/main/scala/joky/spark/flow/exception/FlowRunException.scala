package joky.spark.flow.exception

import joky.spark.flow.FlowNode

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/14 15:17
  * @Description:
  */
class FlowRunException(flowId:Long, flowNodes: Seq[FlowNode], message: String) extends RuntimeException {
    override def toString: String = {
        s"$message, flowId=$flowId, flowNods=$flowNodes"
    }
}
