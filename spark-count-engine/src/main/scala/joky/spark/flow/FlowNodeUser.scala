package joky.spark.flow

import java.sql.{Date, Timestamp}

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/12 21:35
  * @Description:
  */
case class FlowNodeUser(
                           flow_id: Long,
                           flow_node_id: Int,
                           branch_index: Int,
                           stat_time: Timestamp,
                           device_id: String,
                           user_id: String,
                           flow_node_name: String
                       ) {

}
