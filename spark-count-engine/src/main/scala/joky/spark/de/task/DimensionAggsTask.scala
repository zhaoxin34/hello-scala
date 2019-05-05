package joky.spark.de.task

import joky.spark.de.entity.{Agg, ValidResult}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Success, Try}

case class DimensionAggsTask(dimensionColumns: Seq[String], aggs: Seq[Agg], limit: Int = 0) extends Task {

    override def valid: ValidResult = {
        aggs.map(_.valid).filter(!_.success).reduceOption((a, b) => ValidResult(a.success && b.success, a.message + "\n" + b.message)).getOrElse(ValidResult())
    }

    override def toString: String = {
        s"DimensionAggs[columns(${dimensionColumns.mkString(",")}), aggs(${aggs.map(_.toString).mkString(",")}), limit $limit]"
    }

    override def execute(father: Try[DataFrame], spark: SparkSession): Try[DataFrame] = {
        father match {
            case Success(df) =>
                val rltFrame = df.groupBy(dimensionColumns.map(df.col): _*)
                val aggColumns = aggs.map(Task.aggToColumn)
                var rtDf = rltFrame.agg(aggColumns.head, aggColumns.slice(1, aggColumns.size): _*)
                if (limit > 0 )
                    rtDf = rtDf.limit(limit)
                Success(rtDf)
            case f => f
        }
    }
}
