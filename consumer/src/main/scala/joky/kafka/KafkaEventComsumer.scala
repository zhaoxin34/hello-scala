package joky.kafka

import joky.spark.SparkBuilder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/26 13:43
  * @Description:
  */
object KafkaEventComsumer extends App {
    val spark = SparkBuilder.build("Kafka Event Consumer")
    val zkQuorum = "datatist-centos00:2181,datatist-centos01:2181,datatist-centos02:2181"
    val group = "zx-test01"
    val topics = "test-producer-20190103"
    val numThreads = 1
    val sparkConf = new SparkConf().setAppName("EventCounter").setMaster("local")
    val ssc = new StreamingContext(sparkConf, Seconds(2))
    ssc.checkpoint("checkpoint")

//    val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
//    val lines = KafkaUtils.createStream(, zkQuorum, group, topicMap).map(_._2)
//    val words = lines.flatMap(_.split(" "))
//    val wordCounts = words.map(x => (x, 1L))
//        .reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)
//    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
}
