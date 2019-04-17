package joky.spark.count.engine

import joky.core.util.ConfigUtil
import joky.event.creator.EventCreator
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.concurrent.duration._

object Starter extends App {

    val task: Task  = ConfigUtil.readYamlFile("spark-count-engine/src/main/resources/task1.yaml", classOf[Task])
    println(task)

    val spark = SparkSession.builder().master("local[1]").config("spark.sql.shuffle.partitions", "2")
        .appName("sql test").getOrCreate()

    import spark.implicits._
    import org.apache.spark.sql.functions._

    val eventList = EventCreator.createEventList(duration = 100 seconds)

    val eventDs = eventList.toDS()
    eventDs.printSchema()
    eventDs.createGlobalTempView("event")

    spark.sql("select * from global_temp.event limit 10").show(false)
//    eventDs
//            .filter("sessionId like '%a' and eventName = 'order'")
//        .groupBy("sessionId")
//        .agg(count("*").as("acount"), max("eventTime").as("max_event_time"))
//        .sort($"acount".desc)
//        .show(false)


    val t = spark.table(task.from)
        t.filter(task.filter.toCondition)
        val aggColumns: Seq[Column] = task.group.aggs.map(agg => {
            agg.function match {
                case "MAX" => max(agg.column).as(agg.as)
                case "COUNT" => count(agg.column).as(agg.as)
            }
        })
        println(aggColumns)

        val aggTable = t.groupBy(task.group.columns.map(t.col): _*)
                .agg(aggColumns.head, aggColumns.slice(1, aggColumns.size): _*)

        val sortColumns: Seq[Column] = task.group.sorts.map(sort => {
            sort.order match {
                case "DESC" => aggTable.col(sort.column).desc
                case _ => aggTable.col(sort.column).asc
            }
        })
        aggTable.sort(sortColumns: _*)
        aggTable.limit(task.limit)
        aggTable.show(false)
        aggTable
//        .filter(plan.filter.toCondition)
//        .groupBy(plan.group.columns.map())
}
