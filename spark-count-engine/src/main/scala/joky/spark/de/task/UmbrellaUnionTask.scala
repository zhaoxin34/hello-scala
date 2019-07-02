package joky.spark.de.task
import org.apache.spark.sql.{DataFrame, SparkSession}
import joky.spark.de.task.Task._
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.{Failure, Success, Try}

/**
  * 伞状任务，从task出发，每个task接着执行一个umbrellaTask，最后把所有task union在一起
  * @param task
  * @param umbrellaTasks
  */
case class UmbrellaUnionTask(task: Task, umbrellaTasks: Seq[Task]) extends Task {
    override def toString: String = {
        s"Umbrella[$task -> union(\n ${umbrellaTasks.map("\t" + _.toString).mkString("\n")}\n)]"
    }

    override def execute(father: DataFrame, spark: SparkSession): DataFrame = {
        val cur = task.run(Success(father), spark)
        val results = umbrellaTasks
            .map(_.run(cur, spark))

        val fails = results.filter(_.isFailure)

        if (fails.nonEmpty) {
            throw Task.exeception(fails.mkString(","))
        }
        else {
            results.map(_.get).reduceOption((a, b) => a.union(b)) match {
                case Some(df: DataFrame) => df
                case _ => throw Task.exeception("success umbrellaTasks empty")
            }
        }
    }
}

