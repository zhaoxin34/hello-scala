package joky.kafka

import java.util.Objects

import joky.core.bean.Event
import joky.core.util.JsonUtil
import joky.spark.SparkBuilder
import org.apache.spark.examples.streaming.StreamingExamples
import org.apache.spark.sql.{Dataset, ForeachWriter, SparkSession}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/26 13:43
  * @Description:
  */
object KafkaEventComsumer extends App {
    //    def setStreamingLogLevels() {
    //        val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    //        if (!log4jInitialized) {
    //            // We first log something to initialize Spark's default logging, then we override the
    //            // logging level.
    //            println("Setting log level to [WARN] for streaming example." +
    //                " To override add a custom log4j.properties to the classpath.")
    //            Logger.getRootLogger.setLevel(Level.WARN)
    //        }
    //    }
    //
    //    setStreamingLogLevels()

    val spark: SparkSession = SparkBuilder.build("Kafka Event Consumer")

    StreamingExamples.setStreamingLogLevels()
    val kafkaServers = "datatist-centos00:9092,datatist-centos01:9092,datatist-centos02:9092"
    val group = "zx-test02"
    val topicId = "test-producer-20190111"

    val df = spark
        .readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", kafkaServers)
        .option("subscribe", topicId)
        .option("group.id", group)
//        .option("auto.offset.reset", "latest")
        .option("auto.offset.reset", "earliest")
        .option("enable.auto.commit", false: java.lang.Boolean)
        .load()
    //    df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    //        .as[(String, String)]

    import spark.implicits._


    val events: Dataset[Event] = df
        .selectExpr("CAST(value AS STRING)")
        .as[String]
        .map(value => {
            JsonUtil.tryFromJson(value, classOf[Event]) match {
                case scala.util.Success(event) => event
                case scala.util.Failure(_) => null
            }

        }).filter(_ != null)


    events.createTempView("events")

    // not truncate content
    //    events.show(false)

    events.printSchema()

//    val siteEventCount = events.select($"eventTime", $"siteId")
//        .withWatermark("eventTime", "5 minutes")
//        .groupBy(
//            window($"eventTime", "30 seconds", "30 seconds"),
//            $"siteId"
//        )
//        .count()

//        .orderBy($"window")

//    val query = siteEventCount.writeStream
//        // update 模式不支持sort，也就是说.orderBy($"window")这行必须注释掉
//        // 支持complete，不支持append
    // complete支持order by
//        .outputMode("update")
//        .format("console")
//        .option("truncate", "false")
//        .trigger(Trigger.ProcessingTime("30 seconds"))
//        .start()

    val query = events.writeStream.foreach(new ForeachWriter[Event] {
        var partitionId: Long = _
        var version: Long = _

        override def open(partitionId: Long, version: Long): Boolean = {
            this.partitionId = partitionId
            this.version = version
            true
        }

        override def process(value: Event): Unit = {
            println(s"$partitionId, $version =========> " + value)
        }

        override def close(errorOrNull: Throwable): Unit = {

        }
    })
        .trigger(Trigger.ProcessingTime("30 seconds"))
        .start()

    query.awaitTermination()
}
