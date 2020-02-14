package joky.spark.flow

import java.sql.Date

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/12 21:35
  * @Description:
  */
case class FlowNodeUser(
                           flowId: Long,
                           flowNodeId: Int,
                           branchIndex: Int,
                           statTime: Date,
                           deviceId: String,
                           userId: String
                       ) {

}
