package joky.spark.de

import java.util.Date

import joky.spark.de.entity._
import joky.spark.de.entity.helper.TimeUnit
import joky.spark.de.task.DailyMetricChartTask
import org.apache.spark.sql.SparkSession

object Engine extends App {


    val format = "yyyy-MM-dd"
    val simpleFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")

    implicit def stringToDate(date: String): Date = simpleFormat.parse(date)

    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("event test").getOrCreate()

    val df = spark.read.parquet("spark_data/table/zx00_event")
    df.createGlobalTempView("event")
//    spark.sql("select count(*) from global_temp.event").show(false)
    spark.sql("select count(*) from global_temp.event where date = 20190402").show(false)
    spark.sql("select count(*) from global_temp.event where date = 20190403").show(false)
    spark.sql("select count(*) from global_temp.event where date = 20190404").show(false)
    spark.sql("select count(*) from global_temp.event where date = 20190405").show(false)


    val table = Table("global_temp", "event", "事件表")
    val metric = Metric("总数", "COUNT", table.asteriskColumn)
    val metricChart = MetricChart("7日线图", TableMetrics(table, Seq(metric)), "2019-04-01", "2019-04-10", TimeUnit.DAY, Seq(table.column("city", "城市"), table.column("country", "国家")), 5)

    println(metricChart)

    val dailyMetricsChartTask = DailyMetricChartTask(metricChart.tableMetrics.table, metricChart.tableMetrics.metrics, metricChart.startTime, metricChart.endTime, "date", "yyyyMMdd", metricChart.dimensionColumns, metricChart.dimensionLimit)
    println(dailyMetricsChartTask)
    dailyMetricsChartTask.run(spark = spark).get.show(false)

    Thread.sleep(Int.MaxValue)
}
