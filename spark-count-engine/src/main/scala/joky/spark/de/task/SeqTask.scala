package joky.spark.de.task
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util._


/**
  * 顺序执行的任务
  * @param tasks 顺序任务
  */
case class SeqTask(tasks: Task*) extends Task {
    override def toString: String = {
        s"${tasks.map(_.toString).mkString(" -> ")}"
    }

    override def execute(father: DataFrame, spark: SparkSession = null): DataFrame = {
        tasks.foldLeft(Try(father))((a, b) => b.run(a, spark)) match {
            case Success(df) => df
            case Failure(e) => throw e
        }
    }

    override def +(task: Task): Task = {
        task match {
            case st: SeqTask => SeqTask(this.tasks ++ st.tasks: _*)
            case t: Task => SeqTask(this.tasks ++ Seq(t): _*)
        }
    }

    override def ++(tasks: Seq[Task]): Task = {
        SeqTask(this.tasks ++ tasks: _*)
    }
}
