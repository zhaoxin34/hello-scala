package joky.test

import org.apache.spark.sql.SparkSession

/**
  * @Auther: zhaoxin
  * @Date: 2019/1/21 14:04
  * @Description:
  */
object TestMyAverage extends App {
    val spark = SparkSession.builder().master("local").getOrCreate()

    spark.udf.register("myAverage", MyAverage)

    val df = spark.read.json("/Users/zhaoxin/working/life/scala/hello-scala/consumer/src/main/resources/employees.json")
    df.createOrReplaceTempView("employees")
    df.show()

    val result = spark.sql("SELECT myAverage(salary) as average_salary FROM employees")
    result.show()
}
