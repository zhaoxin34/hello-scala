package joky.spark.de

import java.util.Date

import joky.spark.de.entity._
import joky.spark.de.entity.helper.{ColumnType, TimeUnit}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

object Engine extends App {

    val format = new java.text.SimpleDateFormat("yyyy-MM-dd")

    implicit def stringToDate(date: String): Date = format.parse(date)

    val dateFormat = new java.text.SimpleDateFormat("yyyyMMdd")

    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("event test").getOrCreate()

    import org.apache.spark.sql.functions._

    val df = spark.read.parquet("spark_data/table/zx00_event")
    df.createGlobalTempView("event")
//    spark.sql("select count(*) from global_temp.event").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190402").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190403").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190404").show(false)
//    spark.sql("select count(*) from global_temp.event where date = 20190405").show(false)

//    spark.sql("select * from global_temp.event where eventName= 'pageview' limit 10").show(false)

    /**
      * 用于给dataframe增加常量字段
      *
      * @param filter
      * @param column
      */
    case class FilterValueColumn(filter: Filter, column: ValueColumn)

    private def filterTask(task: FilterTask, father: Option[DataFrame] = None): Option[DataFrame] = {
        father match {
            case Some(df) => Some(task.filters.map(_.toCondition).foldLeft(df)((a, b) => a.filter(b)))
            case None => None
        }
    }

    private def aggToColumn(agg: Agg): Column = {
        agg.function match {
            case "MAX" => max(agg.column).as(agg.as)
            case "COUNT" => count(agg.column).as(agg.as)
        }
    }

    private def aggsTask(task: AggsTask, father: Option[DataFrame] = None): Option[DataFrame] = {
        father match {
            case Some(df) => Some(task.aggs.map(aggToColumn).foldLeft(df)((a, b) => a.agg(b)))
            case None => None
        }
    }

    def addValueColumnTask(task: AddValueColumnTask, father: Option[DataFrame]): Option[DataFrame] = {
        father match {
            case Some(df) => Some(task.valueColumns.foldLeft(df)((a, b) => a.withColumn(b.name, typedLit(b.value))))
            case None => None
        }
    }

    def seqTask(task: SeqTask, father: Option[DataFrame]): Option[DataFrame] = {
        task.tasks.foldLeft(father)((a, b) => runTask(b, a))
    }

    def umbrellaUnionTask(task: UmbrellaUnionTask, father: Option[DataFrame]): Option[DataFrame] = {
        val cur = runTask(task.task)
        task.umbrellaTasks
            .flatMap(runTask(_, cur))
            .reduceOption((a, b) => a.union(b))
    }

    def dimensionAggsTask(task: DimensionAggsTask, father: Option[DataFrame]): Option[DataFrame] = {
        father match {
            case Some(df) =>
                val rltFrame = df.groupBy(task.dimensionColumns.map(df.col): _*)
                val aggColumns = task.aggs.map(aggToColumn)
                var rtDf = rltFrame.agg(aggColumns.head, aggColumns.slice(1, aggColumns.size): _*)
                if (task.limit >0 )
                    rtDf = rtDf.limit(task.limit)
                Some(rtDf)
            case None => None
        }
    }

    def fromTask(task: FromTask, spark: SparkSession): Option[DataFrame] = {
        var df = spark.table(task.from)
        if (task.limit > 0)
            df = df.limit(task.limit)
        Some(df)
    }

    def runTask(task: Task, father: Option[DataFrame] = None): Option[DataFrame] = {
        println(task)
        val taskValidResult = task.valid

        if (!taskValidResult.success) {
            println(taskValidResult.message)
            None
        }

        task match {
            case t: FromTask => fromTask(t, spark)
            case t: FilterTask => filterTask(t, father)
            case t: AggsTask => aggsTask(t, father)
            case t: AddValueColumnTask => addValueColumnTask(t, father)
            case t: SeqTask => seqTask(t, father)
            case t: UmbrellaUnionTask => umbrellaUnionTask(t, father)
            case t: DimensionAggsTask => dimensionAggsTask(t, father)
        }
    }

    private def metricsToAggs(metrics: Seq[Metric]): Seq[Agg] = {
        metrics.map(a => Agg(a.aggColumn.name, a.aggFunction, a.label))
    }

    def metricChartToTask(metricChart: MetricChart): Option[Task] = {
        if (metricChart.startTime == null || metricChart.endTime == null) {
            println(s"metricChart.startTime or metricsChart.endTime cannot be null")
            None
        }

        if (metricChart.startTime.getTime > metricChart.endTime.getTime) {
            println(s"metricChart.startTime must less than metricChart.endTime")
            None
        }


        val tasks = ListBuffer[Task]()

        tasks.add(FromTask(s"${metricChart.tableMetrics.table.db}.${metricChart.tableMetrics.table.name}"))


        val dateRange =  metricChart.startTime.getTime to metricChart.endTime.getTime by 1000 * 60 * 60 * 24

        val filterValueColumns = dateRange.map(date => {
            FilterValueColumn(SimpleFilter("date = " + dateFormat.format(new Date(date))), ValueColumn("date", "时间", ColumnType.LONG, date))
        })

        val filtersTasks = filterValueColumns.map(fv =>
            SeqTask(FilterTask(Seq(fv.filter)),
                    AddValueColumnTask(Seq(fv.column)))
        )

        val dimensionTask = UmbrellaUnionTask(SeqTask(tasks: _*), filtersTasks)

        metricChart.dimensionColumns match {
            case Nil => Some(SeqTask(
                dimensionTask,
                AggsTask(metricsToAggs(metricChart.tableMetrics.metrics))
            ))
            case _ => Some(SeqTask(
                dimensionTask,
                DimensionAggsTask(metricChart.dimensionColumns.map(_.name) ++ Seq("date"), metricsToAggs(metricChart.tableMetrics.metrics), metricChart.dimensionLimit)
            ))
        }
    }



    val table = Table("global_temp", "event", "事件表")
    val metric = Metric("总数", "COUNT", table.asteriskColumn)
    val metricChart = MetricChart("7日线图", TableMetrics(table, Seq(metric)), "2019-04-01", "2019-04-10", TimeUnit.DAY, Seq(table.column("city", "城市"), table.column("country", "国家")), 5)

    println(metricChart)

    val task = metricChartToTask(metricChart)
    task.foreach(println)

//    task.foreach(runTask(_, None).foreach(_.show()))
}
