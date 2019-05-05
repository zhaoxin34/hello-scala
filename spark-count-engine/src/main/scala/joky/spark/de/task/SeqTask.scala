package joky.spark.de.task
import org.apache.spark.sql.{DataFrame, SparkSession}
import joky.spark.de.task.Task._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Success, Try}


/**
  * 顺序执行的任务
  * @param tasks 顺序任务
  */
case class SeqTask(tasks: Task*) extends Task {
    override def toString: String = {
        s"${tasks.map(_.toString).mkString(" -> ")}"
    }

    override def execute(father: Try[DataFrame], spark: SparkSession = null): Try[DataFrame] = {
        tasks.foldLeft(father)((a, b) => b.run(a, spark))
    }
}
