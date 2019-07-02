package joky.spark.de.task

import joky.spark.de.entity.{Agg, ValidResult}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Success, Try}

case class AggsTask(aggs: Seq[Agg]) extends Task {

    override def valid: ValidResult = {
        aggs.map(_.valid).filter(!_.success).reduceOption((a, b) => ValidResult(a.success && b.success, a.message + "\n" + b.message)).getOrElse(ValidResult())
    }

    override def toString: String = {
        s"Agg[${aggs.map(_.toString).mkString(",")}]"
    }

    override def execute(father: DataFrame, spark: SparkSession): DataFrame = {
        aggs.map(Task.aggToColumn).foldLeft(father)((a, b) => a.agg(b))
    }
}
