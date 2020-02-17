package joky.spark.flow.spark

import joky.spark.de.entity.Table

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/14 23:24
  * @Description:
  */
case class SparkBusiConfig(flowNodeUserTable: Table = Table("global_temp", "flow_node_user", "")) {

}
