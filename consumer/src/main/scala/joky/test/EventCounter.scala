package joky.test

import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

/**
  * @Auther: zhaoxin
  * @Date: 2018/11/22 16:52
  * @Description:
  */
object EventCounter {
    def main(args: Array[String]) {

//        StreamingExamples.setStreamingLogLevels()

        // val Array(zkQuorum, group, topics, numThreads) = args
        val zkQuorum = "datatist-centos00:2181,datatist-centos01:2181,datatist-centos02:2181"
        val group = "zx-test01"
        val topics = "message-from-prod"
        val numThreads = 1
        val sparkConf = new SparkConf().setAppName("EventCounter").setMaster("local")
        val ssc = new StreamingContext(sparkConf, Seconds(2))
        ssc.checkpoint("checkpoint")

        val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
        val lines = KafkaUtils.createStream(ssc, zkQuorum, group, topicMap).map(_._2)
        val words = lines.flatMap(_.split(" "))
        val wordCounts = words.map(x => (x, 1L))
            .reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)
        wordCounts.print()

        ssc.start()
        ssc.awaitTermination()

        // structure kafka stream
        // see: https://spark.apache.org/docs/2.2.0/structured-streaming-kafka-integration.html
//        val spark = SparkSession.builder().appName("test").master("local").getOrCreate()
//        val df = spark.readStream.format("kafka")
//            .option("kafka.bootstrap.servers", "datatist-centos00:9092,datatist-centos01:9092,datatist-centos02:9092")
//            .option("subscribe", "message-from-prod")
//            .load()
//        df.printSchema()
    }
}
