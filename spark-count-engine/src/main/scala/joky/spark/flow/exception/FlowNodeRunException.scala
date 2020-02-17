package joky.spark.flow.exception

import joky.spark.flow.FlowNode

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/16 21:27
  * @Description:
  */
case class FlowNodeRunException(message: String, flowNode: FlowNode) extends RuntimeException(message) {
    override def toString = s"$message, $flowNode"
}
