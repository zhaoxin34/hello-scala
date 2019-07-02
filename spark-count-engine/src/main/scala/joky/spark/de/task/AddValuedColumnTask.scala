package joky.spark.de.task

import joky.spark.de.entity.{ValidResult, ValueColumn}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Success, Try}

case class AddValuedColumnTask(valueColumns: ValueColumn*) extends Task {

    override def valid: ValidResult = {
        valueColumns match {
            case null | Nil => ValidResult(false, s"ValueColumn can not be empty $valueColumns")
            case _ => super.valid
        }
    }

    override def toString: String = {
        s"AddValuedColumn[${valueColumns.map(_.toString).mkString(",")}]"
    }

    override def execute(father: DataFrame, spark: SparkSession): DataFrame = {
        valueColumns.foldLeft(father)((a, b) => a.withColumn(b.name, typedLit(b.value)))
    }
}