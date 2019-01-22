package joky.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Duration, StreamingContext}

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/26 16:34
  * @Description:
  */
object SparkConfig {
    private val config = Map(

        "spark.eventLog.enabled" -> true,
        "spark.eventLog.overwrite" -> true,
        "spark.eventLog.buffer.kb" -> "10",

        "spark.eventLog.dir" -> "./spark_data/event_log",

        "spark.sql.parquet.compression.codec" -> "snappy",
        // 这个参数和task个数没关系
        "spark.sql.shuffle.partitions" -> 4,

        // 在local模式下没有用
        // "spark.scheduler.mode" -> "FAIR",
        "spark.default.parallelism" -> 10,
        "spark.sql.streaming.checkpointLocation" -> "./spark_data/checkpoint/"

    )
    private val sparkMaster = "local[2]"

    def sparkConf(): SparkConf = {
        val sparkConf: SparkConf = new SparkConf()
        sparkConf.setMaster(sparkMaster)
        config.foreach(a => sparkConf.set(a._1, a._2.toString))
        sparkConf
    }

}

//object SparkMaster extends Enumeration {
//    val LOCAL2 = Value("local[2]")
//    val LOCAL4 = Value("local[4]")
//}

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/26 16:39
  * @Description:
  */
object SparkBuilder {

    def build(appName: String): SparkSession = {
        SparkSession.builder()
            .appName(appName)
            .config(SparkConfig.sparkConf())
            .getOrCreate()
    }

    def buildStreamingContext(appName: String, duration: Duration): StreamingContext = {
        val ss = new StreamingContext(SparkConfig.sparkConf().setAppName(appName), duration)
        ss
    }
}
