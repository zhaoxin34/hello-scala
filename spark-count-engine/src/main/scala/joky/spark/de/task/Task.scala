package joky.spark.de.task

import joky.spark.de.entity.{Agg, Metric, ValidResult}
import org.apache.spark.sql.functions.{count, max}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}



trait Task {
    def valid:ValidResult = ValidResult()

    /**
      * 外部调用的运行接口
      *
      * @param father
      * @param spark
      * @return
      */
    final def run(father: Try[DataFrame] = Success(null), spark: SparkSession): Try[DataFrame] = {
        valid match {
            case ValidResult(true, _) =>
                father match {
                    case Success(df) =>
                        try {
                            Success(execute(df, spark))
                        } catch {
                            case e: Throwable =>
                                e.printStackTrace()
                                Failure(e)
                        }
                    case Failure(e) => Failure(e)
                }

            case ValidResult(false, message) => Failure(new RuntimeException(message))
        }
    }

    /**
      * 内部运行函数
      * @param father
      * @param spark
      * @return
      */
    @throws(classOf[Exception])
    protected def execute(father: DataFrame, spark: SparkSession): DataFrame

    def +(task: Task): Task = {
        SeqTask(this, task)
    }

    def ++(tasks: Seq[Task]): Task = {
        SeqTask(Seq(this) ++ tasks: _*)
    }
}

object Task {
    def aggToColumn(agg: Agg): Column = {
        agg.function match {
            case "MAX" => max(agg.column).as(agg.as)
            case "COUNT" => count(agg.column).as(agg.as)
        }
    }

    def metricsToAggs(metrics: Seq[Metric]): Seq[Agg] = {
        metrics.map(a => Agg(a.aggColumn.name, a.aggFunction, a.label))
    }

    def exeception(message: String): RuntimeException = {
        new RuntimeException(message)
    }
}
















