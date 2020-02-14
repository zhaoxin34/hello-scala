package joky.spark.de.task

import org.apache.spark.sql.{DataFrame, SparkSession}

case class MapTask() extends Task {

    override def toString: String = {
        s"map task"
    }

    override def execute(father: DataFrame, spark: SparkSession): DataFrame = {
        father
    }
}
