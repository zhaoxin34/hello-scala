//package joky.spark
//
//import java.sql.Timestamp
//
//import joky.core.util.SomeUtil
//import org.apache.spark.sql.streaming.Trigger
//import org.apache.spark.sql.{Dataset, SparkSession}
//
///**
//  * @Auther: zhaoxin
//  * @Date: 2018/12/15 15:43
//  * @Description:
//  */
//object SparkStreaming extends App {
//    val port = 9999
//
//
//    SomeUtil.openSocketPrinter(port, p => {
//        val runner = new Runnable {
//            override def run(): Unit = {
//                while(true) {
//                    val line = s"%d,%s,%s,%s".format(
//                        new java.util.Date().getTime,
//                        SomeUtil.randomPick(Event.eventNames).get,
//                        SomeUtil.randomPick(Event.sessionIds).get,
//                        SomeUtil.randomPick(Event.userIds).get)
////                    println(s"write socket $port: $line")
//                    p.println(line)
//                    p.flush()
//                    Thread.sleep(500)
//                }
//            }
//        }
//        new Thread(runner).start()
//    })
//
//    val spark = SparkBuilder.build("streaming test")
//    import spark.implicits._
//
//    val lines = spark.readStream.format("socket").option("host", "localhost").option("port", port).load()
//
////    val words = lines.as[String].flatMap(_.split(" ")).repartition(5)
////    val wordCount = words.groupBy("value").count().repartition(3)
////    wordCount.printSchema()
////    val query = wordCount.writeStream.format("console").outputMode("complete").start()
////    query.awaitTermination()
//
//    val events: Dataset[Event] = lines.as[String].map(_.split(',')).filter(_.length == 4)
//        .map(arr => {
//            Event(new Timestamp(arr(0).toLong), arr(1), arr(2), arr(3))
//        })
//
//    events.createTempView("events")
//
//    // not truncate content
////    events.show(false)
//
//    events.printSchema()
//
//    val query = events
//        .select("sessionId", "eventTime", "eventName", "userId")
//            .groupBy(window($"eventTime", "30 seconds", "30 seconds"), $"sessionId")
//        .writeStream
//        .format("console")
//        .outputMode("append")
//        .trigger(Trigger.ProcessingTime("5 seconds"))
//        .start()
//
////        val query = events
////            .select("sessionId", "eventTime", "eventName", "userId")
////            .writeStream
////            .format("json")
////            .option("path", "./csv/events")
////            .option("checkpointLocation", "./checkout")
////            .outputMode("append")
////            .trigger(Trigger.ProcessingTime("5 seconds"))
////                .start()
//
//    // 有了window可以用watermark，输出模式只能是update
////    val queryGroup = events
////        .sqlContext.sql("select window(eventTime, \"10 seconds\") as session_window, count(*) count, sessionId from events group by window(eventTime, \"10 seconds\"), sessionId")
////        .withWatermark("session_window", "60 seconds")
////        .writeStream
////        .format("console")
//////        .outputMode("complete")
//////        .outputMode("append")
////        .outputMode("update")
////        .trigger(Trigger.ProcessingTime("10 seconds"))
////        .start()
//
//    // 如下代码在任何输出模式下都是错误的
////    val countQuery = events.sqlContext.sql("select * from events order by eventTime asc")
////            .writeStream
////            .format("console")
////            .outputMode("complete")
////            .trigger(Trigger.ProcessingTime("10 seconds"))
////            .start()
//
//
//    spark.streams.awaitAnyTermination()
////    query.awaitTermination()
//}
