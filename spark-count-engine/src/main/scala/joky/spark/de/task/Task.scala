package joky.spark.de.task

import joky.spark.de.entity.{Agg, Metric, ValidResult}
import org.apache.spark.sql.functions.{count, max}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}



trait Task {
    def valid:ValidResult = ValidResult()

    def showError(dataFrame: Try[DataFrame]): Unit = {
        dataFrame match {
            case Failure(exception) =>
                print(s"$this exception[$exception]")
            case _ =>
        }
    }

    def run(father: Try[DataFrame] = Success(null), spark: SparkSession): Try[DataFrame] = {
        valid match {
            case ValidResult(true, _) =>
                val result = execute(father, spark)
                showError(result)
                result

            case ValidResult(false, message) => Failure(new RuntimeException(message))
        }
    }

    protected def execute(father: Try[DataFrame] = Success(null), spark: SparkSession): Try[DataFrame]
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
















