package joky.spark.de.task

import joky.spark.de.entity.{Column, TableColumn, ValueColumn}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

case class SelectTask(columns: Column*) extends Task {

    override protected def execute(father: DataFrame, spark: SparkSession): DataFrame = {
        val sparkColumns = columns.map {
            case tc: TableColumn => father.col(tc.name)
            case vc: ValueColumn => typedLit(vc.value).as(vc.name)
        }

        father.select(sparkColumns:_*)
    }

    override def toString: String = {
        val columnsString = columns.map({
            case tc: TableColumn => tc.name
            case vc: ValueColumn => s"${vc.value} as ${vc.name} "
        }).mkString(",")
        s"Select[$columnsString]"
    }
}
