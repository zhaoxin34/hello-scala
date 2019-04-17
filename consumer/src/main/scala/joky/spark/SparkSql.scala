package joky.spark

import java.sql.Timestamp

import joky.core.bean.Event
import joky.core.util.SomeUtil
import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/24 17:39
  * @Description:
  *              生成文件的个数等于partition个数 * bucket数，repartition * bucket
  *              更丰富的值知识在下面这个文章中
  *              https://stackoverflow.com/questions/48585744/why-is-spark-saveastable-with-bucketby-creating-thousands-of-files
  */
object SparkSql extends App {

    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("sql test").getOrCreate()

//    import spark.implicits._
//
//    val eventList = Range(0, 100).map(i => new Event(new java.util.Date().getTime,
//        SomeUtil.randomPick(Event.eventNames).get,
//        SomeUtil.randomPick(Event.sessionIds).get,
//        SomeUtil.randomPick(Event.userIds).get))
//
//    val eventDs = eventList.toDS()
//    eventDs.printSchema()
//
//    eventDs.createGlobalTempView("event")
//
//    spark.sql("select * from global_temp.event").show(false)
//
//    eventDs.repartition(1).write.partitionBy("eventName").bucketBy(3, "userId")
//        .sortBy("userId")
//        .format("json").option("path", "./data/event")
//        .mode(SaveMode.Overwrite)
//        .saveAsTable("event")

    Thread.sleep(1000* 1000 * 1000)

}
