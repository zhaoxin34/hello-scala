package joky.spark.de.task

import joky.spark.de.entity.{Filter, ValidResult}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Success, Try}

case class FilterTask(filters: Filter*) extends Task {

    override def valid: ValidResult = {
        filters.map(_.valid).filter(!_.success).reduceOption((a, b) => ValidResult(a.success && b.success, a.message + "\n" + b.message)).getOrElse(ValidResult())
    }

    override def toString: String = {
        s"Filter[${filters.map(_.toCondition).mkString(",")}]"
    }

    override def execute(father: Try[DataFrame], spark: SparkSession): Try[DataFrame] = {
        father match {
            case Success(df) => Success(filters.map(_.toCondition).foldLeft(df)((a, b) => a.filter(b)))
            case f => f
        }
    }
}

