package joky.spark.de

import java.util.Date

import joky.spark.de.entity.{Metric, MetricChart, Table, TableMetrics}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.sql.functions.{count, max}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}
import org.joda.time.DateTimeUtils
import scala.collection.JavaConversions._

object Engine extends App {

    val format = new java.text.SimpleDateFormat("yyyy-MM-dd")

    implicit def stringToDate(date: String): Date = format.parse(date)

    val dateFormat = new java.text.SimpleDateFormat("yyyyMMdd")

    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("event test").getOrCreate()

    import spark.implicits._
    import org.apache.spark.sql.functions._

    val df = spark.read.parquet("spark_data/table/zx00_event")
    df.createGlobalTempView("event")
//    spark.sql("select count(*) from global_temp.event").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190402").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190403").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190404").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190405").show(false)

//    spark.sql("select * from global_temp.event where eventName= 'pageview' limit 10").show(false)


    def runTask(task: Task): Option[DataFrame] = {
        println(task)
        val taskValidResult = task.valid

        if (!taskValidResult.success) {
            println(taskValidResult.message)
            None
        }

        var t = spark.table(task.from)
        if (!task.filter.isNone)
            t = t.filter(task.filter.toCondition)

        val aggColumns: Seq[Column] = task.group.aggs.map(agg => {
            agg.function match {
                case "MAX" => max(agg.column).as(agg.as)
                case "COUNT" => count(agg.column).as(agg.as)
            }
        })

        val aggTable = task.group.columns match {
            case Seq(_) => t.groupBy(task.group.columns.map(t.col): _*)
                .agg(aggColumns.head, aggColumns.slice(1, aggColumns.size): _*)
            case _ => t.agg(aggColumns.head, aggColumns.slice(1, aggColumns.size): _*)
        }


        if (task.group.sorts != null &&  task.group.sorts.nonEmpty) {
            val sortColumns: Seq[Column] = task.group.sorts.filter(!_.isNone).map(sort => {
                sort.order match {
                    case "DESC" => aggTable.col(sort.column).desc
                    case _ => aggTable.col(sort.column).asc
                }
            })
            aggTable.sort(sortColumns: _*)
        }

        if (task.limit > 0)
            aggTable.limit(task.limit)

        aggTable.show(false)

        Option(aggTable)
    }

    def runUnionTask(unionTask: Option[UnionTask]): Option[DataFrame] = unionTask match {
        case Some(ut: UnionTask) => ut.taskList.flatMap(runTask).reduceOption((a, b) => a.union(b))
        case None => None
    }

    def metricChartToTask(metricChart: MetricChart): Option[UnionTask] = {
        if (metricChart.startTime == null || metricChart.endTime == null) {
            println(s"metricChart.startTime or metricsChart.endTime cannot be null")
            None
        }

        if (metricChart.startTime.getTime > metricChart.endTime.getTime) {
            println(s"metricChart.startTime must less than metricChart.endTime")
            None
        }

        val dateRange =  metricChart.startTime.getTime to metricChart.endTime.getTime by 1000 * 60 * 60 * 24
        val tasks = dateRange.map(date => {
            Task(
                metricChart.name,
                s"${metricChart.tableMetrics.table.db}.${metricChart.tableMetrics.table.name}",
                Filter.simpleFilter("date = " + dateFormat.format(new Date(date))),
                Group(null, metricChart.tableMetrics.metrics.map(a => Agg(a.aggColumn.name, a.aggFunction, a.label)), null)
            )
        })

        Option(UnionTask(tasks))
    }



    val table = Table("global_temp", "event", "事件表")
    val metric = Metric("总数", "COUNT", table.asteriskColumn)
    val metricChart = MetricChart("7日线图", TableMetrics(table, Seq(metric)), "2019-04-01", "2019-04-10")

    println(metricChart)

    val unionTask = metricChartToTask(metricChart)

    val dataFrame = runUnionTask(unionTask)

    dataFrame.foreach(_.show())
}
