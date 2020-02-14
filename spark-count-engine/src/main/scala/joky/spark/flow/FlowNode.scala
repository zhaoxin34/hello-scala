package joky.spark.flow

import joky.spark.de.task.Task

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/11 20:27
  * @Description:
  */
case class FlowNode(nodeId: Int,
                    name: String,
                    flowNodeBusiNode: FlowNodeBusiNode,
                    fatherIds: Seq[Int] = Seq(),
                    branchIndex: Int = 0) {
    def getTask(): Task = flowNodeBusiNode.getTask()

}
