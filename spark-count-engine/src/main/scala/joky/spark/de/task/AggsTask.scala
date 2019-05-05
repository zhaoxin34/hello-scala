package joky.spark.de.task

import joky.spark.de.entity.{Agg, ValidResult}
import joky.spark.de.task.Task._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

case class AggsTask(aggs: Seq[Agg]) extends Task {

    override def valid: ValidResult = {
        aggs.map(_.valid).filter(!_.success).reduceOption((a, b) => ValidResult(a.success && b.success, a.message + "\n" + b.message)).getOrElse(ValidResult())
    }

    override def toString: String = {
        s"Agg[${aggs.map(_.toString).mkString(",")}]"
    }

    override def execute(father: Try[DataFrame], spark: SparkSession = null): Try[DataFrame] = {
        father match {
            case Success(df) => Success(aggs.map(Task.aggToColumn).foldLeft(df)((a, b) => a.agg(b)))
            case f => f
        }
    }
}
