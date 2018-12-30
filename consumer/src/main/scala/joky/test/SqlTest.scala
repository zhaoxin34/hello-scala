package joky.test

import org.apache.spark.sql.SparkSession

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/26 16:34
  * @Description:
  */
object SqlTest {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession
            .builder()
            .master("local")
            .appName("sql test").config("abc", "123").getOrCreate()
        val df = spark.read.json("src/main/resources/people.json")
        df.show
        df.printSchema
        df.createOrReplaceTempView("people")
        spark.catalog.refreshTable("people")
        spark.sql("select count(*), name from people group by name").show()
    }
}
