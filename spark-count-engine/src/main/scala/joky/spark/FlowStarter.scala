package joky.spark

import java.util.concurrent.TimeUnit

import joky.spark.de.entity.{SimpleFilter, Table}
import joky.spark.flow._
import joky.spark.flow.spark.{SparkBusiConfig, SparkFlow, SparkFlowNode}
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/11 20:30
  * @Description:
  */
object FlowStarter extends App with Logging {
    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("Campaign Flow").getOrCreate()


    val sparkBusiConfig = SparkBusiConfig()

    spark.read.format("csv").option("header", "true").load("spark_data/table/user/usercenter_user.csv").createGlobalTempView("users")

    val flowNodeUserSchema = StructType(Array(
        StructField("flow_id", LongType, true),
        StructField("flow_node_id", IntegerType, true),
        StructField("branch_index", IntegerType, true),
        StructField("stat_time", TimestampType, true),
        StructField("device_id", StringType, true),
        StructField("user_id", StringType, true),
        StructField("flow_node_name", StringType, true),
        StructField("timeout_minute", IntegerType, true)
    ))

    spark.read.format("csv").option("header", "true")
//        .option("delimiter", "\t")
//        .option("timestampFormat", "yyyy/MM/dd HH:mm:ss")
        .schema(flowNodeUserSchema)
        .load("spark_data/table/flow_node_user/flow_node_user1.csv")
        .createGlobalTempView(sparkBusiConfig.flowNodeUserTable.name)

    val eventSchema = StructType(Array(
        StructField("event_name", StringType, true),
        StructField("user_id", StringType, true),
        StructField("event_time", TimestampType, true)
    ))

    spark.read.format("csv")
        .schema(eventSchema)
        .option("header", "true").load("spark_data/table/event/event.csv").createGlobalTempView("event")

    val flow = SparkFlow(1,
        Seq(
            SparkFlowNode(1, "用户", UserEntryNode(SimpleFilter("email like '%datatist%'")), childrenIds = Seq(2)),
            SparkFlowNode(2, "邮件", MailFunctionNode("周末大促", "肺炎来啦"), fatherIds = Seq(1), childrenIds = Seq(3)),
            SparkFlowNode(3, "10 hour", WaitTimerNode(10, TimeUnit.HOURS), fatherIds = Seq(2), childrenIds = Seq(4)),
            SparkFlowNode(4, "判断加入购物车", EventTriggerSplitNode("加入购物车"), fatherIds = Seq(3), childrenIds = Seq(5, 6)),
            SparkFlowNode(5, "推送优惠卷", AppPushFunctionNode("武汉肺炎优惠券"), fatherIds = Seq(4), childrenIds = Seq(8), branchIndex = 0),
            SparkFlowNode(6, "邮件推送", MailFunctionNode("邮件推送新品", "没有解药"), fatherIds = Seq(4), childrenIds = Seq(7), branchIndex = 1),
            SparkFlowNode(7, "5 hour", WaitTimerNode(5, TimeUnit.HOURS), fatherIds = Seq(6), childrenIds = Seq(8)),
            SparkFlowNode(8, "join", JoinNode(), fatherIds = Seq(5, 7), childrenIds = Seq(9)),
            SparkFlowNode(9, "10 hour", WaitTimerNode(10, TimeUnit.HOURS), fatherIds = Seq(8), childrenIds = Seq(10)),
            SparkFlowNode(10, "判断购买", EventTriggerSplitNode("购买"), fatherIds = Seq(9), childrenIds = Seq(11, 12)),
            SparkFlowNode(11, "发送通知", AppPushFunctionNode("上4万了"), fatherIds = Seq(10), branchIndex = 0),
            SparkFlowNode(12, "进一步优惠券", AppPushFunctionNode("优惠券指给没得病的"), Seq(10), branchIndex = 1)
        ),
        FlowRunConfig(10, TimeUnit.SECONDS, 2),
        spark,
        sparkBusiConfig)

    flow.start()
}
