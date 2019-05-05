package joky.spark.de.task

import joky.spark.de.entity.{Sort, Table, ValidResult}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Success, Try}

case class FromTableTask(table: Table, limit: Int = 0, sorts: Seq[Sort] = Seq()) extends Task {

    override def valid: ValidResult = {
        this match {
            case FromTableTask(t: Table, _, _) if !t.valid.success => ValidResult(false, s"table is not valid $t")
            case FromTableTask(_, l, _) if limit < 0 => ValidResult(false, s"limit less than 0, $l")
            case FromTableTask(_, _, null) => ValidResult(false, s"sorts is null")
            case _ => ValidResult()
        }

    }

    override def toString: String = {
        s"From[$table limit $limit order by ${sorts.map(_.toString).mkString(",")}]"
    }

    override def execute(father: Try[DataFrame], spark: SparkSession): Try[DataFrame] = {
        var df = spark.table(table.toString)
        if (limit > 0)
            df = df.limit(limit)
        Success(df)
    }
}

