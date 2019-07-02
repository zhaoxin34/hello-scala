package joky.spark.de.task

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

import joky.spark.de.entity._
import joky.spark.de.entity.helper.{ColumnType, OperatorType, TimeUnit}
import org.apache.commons.lang3.time.DateUtils
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success}

case class FunnelTask(table: Table,
                      filter: Filter,
                      firstStepStartTime: Date,
                      firstStepEndTime: Date,
                      convertTime: Int,
                      convertTimeUnit: TimeUnit,
                      aggColumn: Column,
                      steps: Seq[Step],
                      dateColumn: Column,
                      exportDateFormat: String = "yyyyMMdd",
                      eventTimeColumn: Column
                     ) extends Task {

    val dateColumnFormatter: SimpleDateFormat = new SimpleDateFormat(exportDateFormat)

    override def valid: ValidResult = {
        this match {
            case FunnelTask(t, _, _, _, _, _, _, _, _, _, _) if t == null || !t.valid.success => ValidResult(false, s"table not valid $t")
            case FunnelTask(_, f, _, _, _, _, _, _, _, _, _) if f != null && !f.valid.success => ValidResult(false, s"filter not valid $f")
            case FunnelTask(_, _, f, l, _, _, _, _, _, _, _) if f == null || l == null => ValidResult(false,  s"firstStepStartTime or firstStepEndTime cannot be null")
            case FunnelTask(_, _, f, l, _, _, _, _, _, _, _) if l.getTime <= f.getTime => ValidResult(false,  s"firstStepStartTime must less than firstStepEndTime")
            case FunnelTask(_, _, _, _, ct, _, _, _, _, _, _) if ct <= 0 => ValidResult(false,  s"firstStepStartTime must less than firstStepEndTime")
            case FunnelTask(_, _, _,  _, _, _, ag, _, _, _, _) if ag == null => ValidResult(false,  s"aggColumn can not be empty")
            case FunnelTask(_, _, _,  _, _, _, _, st, _, _, _) if st == null || st.size <= 1 => ValidResult(false,  s"steps size must >= 2")
            case FunnelTask(_, _, _,  _, _, _, _, st, _, _, _) if st.map(_.valid).exists(!_.success) => ValidResult(false,  s"steps exists invalid step $st")
            case _ => super.valid
        }
    }

    private def createSelectStepTask(stepNo: Int = 0): Task = {
        AddValuedColumnTask(ValueColumn("step", "step", ColumnType.INTEGER, stepNo)) + SelectTask(eventTimeColumn, aggColumn)
    }

    /**
      * 创建第一步的任务
      * 过滤开始时间结束时间的分区字段
      * 过滤开始和结束时间
      *
      * @return
      */
    private def createFirstStepTask(): Task = {
        FilterTask(
            BiConnectedFilter(FilterConnector.AND,
                Filter.createSimpleFilter(dateColumn, OperatorType.GTE, Option apply dateColumnFormatter.format(firstStepStartTime)),
                Filter.createSimpleFilter(dateColumn, OperatorType.LTE, Option apply dateColumnFormatter.format(firstStepEndTime))
            )) +
            FilterTask(
            BiConnectedFilter(FilterConnector.AND,
                Filter.createSimpleFilter(eventTimeColumn, OperatorType.GTE, Option apply firstStepStartTime),
                Filter.createSimpleFilter(eventTimeColumn, OperatorType.LTE, Option apply firstStepEndTime)
            )) +
            FilterTask(
                steps.head.filter
            ) +
            createSelectStepTask()
    }

    /**
      * 创建通用任务
      *
      * @return
      */
    private def createCommonTask: Task = {
        val tasks = ArrayBuffer[Task]()
        tasks += FromTableTask(table)
        if (filter != null)
            tasks += FilterTask(filter)
        SeqTask(tasks:_*)
    }

    /**
      * 创建除第一步之后的余下的步骤
      *
      * @return
      */
    private def createTheLeftStepTasks(): Seq[Task] = {
        val lastDateTime = DateUtils.addSeconds(firstStepEndTime, convertTime * convertTimeUnit.getSenconds)

        val task = FilterTask(
            BiConnectedFilter(FilterConnector.AND,
                Filter.createSimpleFilter(dateColumn, OperatorType.GTE, Option apply dateColumnFormatter.format(firstStepStartTime)),
                Filter.createSimpleFilter(dateColumn, OperatorType.LTE, Option apply dateColumnFormatter.format(lastDateTime))
            )) + FilterTask(
            BiConnectedFilter(FilterConnector.AND,
                Filter.createSimpleFilter(eventTimeColumn, OperatorType.GTE, Option apply firstStepStartTime),
                Filter.createSimpleFilter(eventTimeColumn, OperatorType.LTE, Option apply lastDateTime)
            ))

        steps.zipWithIndex
            .drop(1)
            .map {
                case (step, index) => task + FilterTask(step.filter) + createSelectStepTask(index)
            }
    }

    private def createInnerTask(): Task = {
        val commonTask = createCommonTask
        val firstStepTask = createFirstStepTask()
        val leftStepTasks = createTheLeftStepTasks()

        val funnelReadyTask = UmbrellaUnionTask(commonTask, Seq(firstStepTask) ++ leftStepTasks)
        funnelReadyTask
    }

    private val innerTask = createInnerTask()

    override def toString: String = {
        s"FunnelTask[$innerTask]"
    }

    override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
        innerTask.run(Success(father), spark) match {
            case Success(df) => df
            case Failure(e) => throw e
        }
    }
}
