package joky.spark

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import joky.spark.de.entity.{SimpleFilter, Table}
import joky.spark.flow._
import org.apache.spark.sql.SparkSession

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/11 20:30
  * @Description:
  */
object FlowStarter extends App {
    val flow = Flow(Seq(
        FlowNode(1, "用户", UserEntryNode(Table("global_temp", "users", "用户表"), SimpleFilter("email like '%datatist%'"))),
        FlowNode(2, "邮件", MailFunctionNode("周末大促", "肺炎来啦"), Seq(1)),
        FlowNode(3, "10 hour", WaitTimerNode(10, TimeUnit.HOURS), Seq(2)),
        FlowNode(4, "判断加入购物车", EventTriggerSplitNode("加入购物车"), Seq(3)),
        FlowNode(5, "推送优惠卷", AppPushFunctionNode("武汉肺炎优惠券"), Seq(4), 0),
        FlowNode(6, "邮件推送", MailFunctionNode("邮件推送新品", "没有解药"), Seq(4), 1),
        FlowNode(7, "5 hour", WaitTimerNode(5, TimeUnit.HOURS), Seq(6)),
        FlowNode(8, "join", JoinNode(), Seq(5, 7)),
        FlowNode(9, "10 hour", WaitTimerNode(10, TimeUnit.HOURS), Seq(8)),
        FlowNode(10, "判断购买", EventTriggerSplitNode("购买"), Seq(9)),
        FlowNode(11, "发送通知", AppPushFunctionNode("上4万了"), Seq(10)),
        FlowNode(12, "进一步优惠券", AppPushFunctionNode("优惠券指给没得病的"), Seq(10))
    ))


    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("Campaign Flow").getOrCreate()

    spark.sparkContext.setLogLevel("warn")

    import org.apache.spark.sql.functions._
    import spark.implicits._

    val datetimeFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    implicit def stringToDate(date: String): java.util.Date = datetimeFormatter.parse(date)

    val callTime = new Timestamp(stringToDate("2020-02-12 00:00:00").getTime)

    val userDf = spark.read.format("csv").option("header", "true").load("spark_data/table/user/usercenter_user.csv")
    //    userDf.createGlobalTempView("users")


    val flowNodeUserDf = spark.read.format("csv").option("header", "true").load("spark_data/table/flow_node_user/flow_node_user1.csv")
    //    flowNodeUserDf.createGlobalTempView("flow_node_user")

    val userEntryNodeDf = userDf.filter("email like '%datatist%'")
        .join(flowNodeUserDf, userDf.col("user_id") === flowNodeUserDf.col("user_id"), "left_outer")
        .where("flow_node_id is null")
        .select(userDf.col("user_id"), userDf.col("mobile").as("device_id"))
        .withColumn("flow_node_id", typedLit[Int](1))
        .withColumn("branch_index", typedLit[Int](0))
        .withColumn("stat_time", typedLit[Timestamp](callTime))
        .withColumn("timeout_minute", typedLit[Int](0))
        .withColumn("flow_node_name", typedLit[String]("userEntry"))
    //        .withColumn("device_id", typedLit[String](""))
    //            .show(10)

    userEntryNodeDf.show(10)

    val mailFunctionNodeDf = userEntryNodeDf.where("flow_node_id == 1")
        .withColumn("flow_node_id", typedLit[Int](2))
        .withColumn("stat_time", typedLit[Timestamp](callTime))
        .withColumn("flow_node_name", typedLit[String]("mailFunction"))

    mailFunctionNodeDf.show(10)

    val waitTimer10HourNode = mailFunctionNodeDf.where("flow_node_id == 2")
        .withColumn("flow_node_id", typedLit[Int](3))
        .withColumn("stat_time", typedLit[Timestamp](callTime))
        .withColumn("timeout_minute", $"timeout_minute" + 10 * 60)
        .withColumn("flow_node_name", typedLit[String]("waitTimer10Hour"))

    waitTimer10HourNode.show(10)

    val eventDf = spark.read.format("csv").option("header", "true").load("spark_data/table/event/event.csv")
            .where($"event_time" > new Timestamp(System.currentTimeMillis() - 86400000))
            .where("event_name == '加入购物车'")
    eventDf.show(10)

    val eventTriggerSplitNodeDf1 = waitTimer10HourNode
        .join(eventDf, waitTimer10HourNode.col("user_id") === eventDf.col("user_id"), "left_outer")
        .select(waitTimer10HourNode.col("user_id"), $"device_id", $"timeout_minute")
        .withColumn("flow_node_id", typedLit[Int](4))
        .withColumn("branch_index", typedLit[Int](0))
        .withColumn("stat_time", typedLit[Timestamp](new Timestamp(System.currentTimeMillis())))
        .withColumn("flow_node_name", typedLit[String]("加入购物车eventTrigger"))


    val eventTriggerSplitNodeDf1Cache = eventTriggerSplitNodeDf1.cache()

    val eventTriggerSplitNodeDf1Br0 = eventTriggerSplitNodeDf1Cache.where("event_time is not null")
        .withColumn("branch_index", typedLit[Int](0))
        .withColumn("timeout_minute", typedLit[Int](0))
    eventTriggerSplitNodeDf1Br0.show(10)


    val eventTriggerSplitNodeDf1Br1 = eventTriggerSplitNodeDf1Cache.where("(unix_timestamp(current_timestamp()) - unix_timestamp(stat_time)) > timeout_minute")
        .withColumn("branch_index", typedLit[Int](1))
        .withColumn("timeout_minute", typedLit[Int](0))
    eventTriggerSplitNodeDf1Br1.show(10)

    val joinDf = eventTriggerSplitNodeDf1Br0.union(eventTriggerSplitNodeDf1Br1)
        .withColumn("flow_node_id", typedLit[Int](5))
        .withColumn("branch_index", typedLit[Int](0))

    joinDf.show(10)


    //    def createUserFlowDf(): DataFrame = {
    //        val schema = StructType(
    //            Seq(
    //                StructField("flow_id", IntegerType, false),
    //                StructField("flow_node_id", IntegerType, false),
    //                StructField("branch_index", IntegerType, false),
    //                StructField("stat_time", TimestampType, false),
    //                StructField("user_id", StringType, false),
    //                StructField("device_id", StringType, false),
    //            )
    //        )
    //    }


    //    val userFlowNodeDf = userDf.withColumn("flow_id", typedLit[Long](1))
    //        .withColumn("flow_node_id", typedLit[Seq[Int]](Seq(0)))
    //        .withColumn("branch_index", typedLit[Int](0))
    //        .withColumn("stat_time", typedLit[Date](new Date(System.currentTimeMillis())))
    //        .withColumn("device_id", typedLit[String](""))
    //
    //    val addFlowNodeId = udf((seq: Seq[Int], nodeId: Int) => {
    //        seq :+ nodeId
    //    })
    //
    //    val finalDf = userFlowNodeDf
    //        .filter("email like '%datatist%'")
    //        .withColumn("flow_node_id", addFlowNodeId($"flow_node_id", lit(1)))
    //        .filter($"flow_node_id".as[Seq[Int]])
    //            .map(row => {
    ////                val flowNodeIds = row.getSeq(userFlowNodeDf.schema.fieldIndex("flow_node_id"))
    ////                println(flowNodeIds)
    //                row
    //            })


    //    finalDf.show(10)
    //    userDf.createGlobalTempView("users")

}
