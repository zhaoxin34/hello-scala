package joky.spark.flow

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/11 20:40
  * @Description:
  */
object FlowNodeJoinType extends Enumeration {
    type JoinType = Value
    val ENTRY, SPLIT, FUNCTION, END, JOIN = Value
}
