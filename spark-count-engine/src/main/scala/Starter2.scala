import joky.spark.count.engine.project.config.ProjectConfig
import org.apache.spark.sql.SparkSession

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/16 10:25
  * @Description:
  */
object Starter2 extends App {
//    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
//        .appName("event test").getOrCreate()
//
//    import spark.implicits._
//    import org.apache.spark.sql.functions._
//
//    val df = spark.read.parquet("spark_data/table/zx00_event")
//    df.createGlobalTempView("event")
//    spark.sql("select * from global_temp.event where eventName= 'pageview' limit 10").show(false)


    val projectConfig = ProjectConfig.buildConfig("spark-count-engine/src/main/resources/project/project1.yaml")
    println(projectConfig)
}
