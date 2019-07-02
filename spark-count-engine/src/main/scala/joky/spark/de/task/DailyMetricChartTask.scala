package joky.spark.de.task

import java.text.SimpleDateFormat
import java.util.Date

import joky.spark.de.entity._
import joky.spark.de.entity.helper.ColumnType
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

/**
  * 用于给dataframe增加常量字段
  *
  * @param filter
  * @param column
  */
case class FilterValueColumn(filter: Filter, column: ValueColumn)

case class DailyMetricChartTask(table: Table,
                                metrics: Seq[Metric],
                                startTime: Date,
                                endTime: Date,
                                dateColumn: String = "date",
                                exportDateFormat: String = "yyyyMMdd",
                                dimensionColumns: Seq[Column] = Seq(),
                                dimensionLimit: Int = 0) extends Task {

    val simpleExportDateFormat = new SimpleDateFormat(exportDateFormat)

    override def valid: ValidResult = {
        if (startTime == null || endTime == null) {
            ValidResult(false, s"startTime or endTime cannot be null")
        }
        else if (startTime.getTime > endTime.getTime) {
            ValidResult(false, s"startTime must less than endTime")
        }
        else
            super.valid
    }

    /**
      * 创建某天的任务
      *
      * @param dateFilterValueColumn
      * @return
      */
    private def createOneDayTask(dateFilterValueColumn: FilterValueColumn): Task = {
        val metricTask = dimensionColumns match {
            case Nil => AggsTask(Task.metricsToAggs(metrics))
            case _ => DimensionAggsTask(dimensionColumns.map(_.name), Task.metricsToAggs(metrics), dimensionLimit)
        }

        val filterTask = FilterTask(dateFilterValueColumn.filter)

        val addValuedColumnTask = AddValuedColumnTask(dateFilterValueColumn.column)

        SeqTask(filterTask, metricTask, addValuedColumnTask)
    }

    /**
      * 创建内部任务
      *
      * @return
      */
    private def createInnerTask: Task = {
        val tasks = ListBuffer[Task]()
        tasks += FromTableTask(table)

        val dateRange = startTime.getTime to endTime.getTime by 1000 * 60 * 60 * 24

        val filterValueColumns = dateRange.map(date => {
            FilterValueColumn(SimpleFilter(s"$dateColumn = " + simpleExportDateFormat.format(new Date(date))), ValueColumn(dateColumn, "时间", ColumnType.LONG, date))
        })

        UmbrellaUnionTask(SeqTask(tasks: _*), filterValueColumns.map(createOneDayTask))
    }

    private val innerTask = createInnerTask

    override def execute(father: DataFrame, spark: SparkSession): DataFrame = {
        innerTask.run(Success(father), spark) match {
            case Success(df) => df
            case Failure(e) => throw e
        }
    }

    override def toString: String = {
        s"DailyMetricChartTask[$innerTask]"
    }
}
