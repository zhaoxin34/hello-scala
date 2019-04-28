package joky.spark.count.engine

import joky.core.util.SomeUtil
import org.apache.spark.sql.SparkSession

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/26 14:00
  * @Description:
  */
object Starter3 extends App {
    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("event test").getOrCreate()

    import spark.implicits._
    import org.apache.spark.sql.functions._

    case class Employee(id: Int, name: String, gender: Int, salary: Int, department: String)

    val ds = spark.sqlContext.sparkContext.parallelize(
        Seq(
            Employee(1, "a0", SomeUtil.randomPick(Seq(0,1)).get, 1, "d0"),
            Employee(2, "a1", SomeUtil.randomPick(Seq(0,1)).get,3, "d0"),
            Employee(3, "a2", SomeUtil.randomPick(Seq(0,1)).get,2, "d0"),
            Employee(4, "a3", SomeUtil.randomPick(Seq(0,1)).get,4, "d1"),
            Employee(5, "a4", SomeUtil.randomPick(Seq(0,1)).get,6, "d1"),
            Employee(6, "a5", SomeUtil.randomPick(Seq(0,1)).get,7, "d1"),
            Employee(7, "a6", SomeUtil.randomPick(Seq(0,1)).get,8, "d2"),
            Employee(8, "a7", SomeUtil.randomPick(Seq(0,1)).get,9, "d2"),
            Employee(9, "a8", SomeUtil.randomPick(Seq(0,1)).get,10, "d2"),
            Employee(10, "a9", SomeUtil.randomPick(Seq(0,1)).get,11, "d3"),
            Employee(11, "a10", SomeUtil.randomPick(Seq(0,1)).get,12, "d3"),
            Employee(12, "a01", SomeUtil.randomPick(Seq(0,1)).get,13, "d3"),
            Employee(13, "a02", SomeUtil.randomPick(Seq(0,1)).get,14, "d4"),
            Employee(14, "a03", SomeUtil.randomPick(Seq(0,1)).get,15, "d4"),
            Employee(15, "a04", SomeUtil.randomPick(Seq(0,1)).get,16, "d5"),
            Employee(16, "a05", SomeUtil.randomPick(Seq(0,1)).get,17, "d5"),
            Employee(17, "a06", SomeUtil.randomPick(Seq(0,1)).get,18, "d5"),
            Employee(18, "a07", SomeUtil.randomPick(Seq(0,1)).get,19, "d4"),
            Employee(19, "a08", SomeUtil.randomPick(Seq(0,1)).get,20, "d4")
        )
    ).toDS()
    ds.show()
    ds.printSchema()

    ds.createGlobalTempView("emp")

    spark.sql("select coalesce(department, 'All Department') as Department, coalesce(gender, 'All gender') as all_gender, sum(salary) as salary_sum from global_temp.emp group by rollup(department, gender)").show(false)

}
