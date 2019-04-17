package joky.spark.count.engine


import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import org.codehaus.jackson.`type`.TypeReference
import org.codehaus.jackson.annotate.{JsonProperty, JsonSubTypes, JsonTypeInfo}
import org.codehaus.jackson.annotate.JsonSubTypes.Type

case class Filter(connector: String, conditions: Seq[String]) {
    def toCondition: String = {
        conditions.mkString(s" $connector ")
    }
}

case class Agg(column: String, function: String, as: String)

case class Sort(column: String, order: String)

case class Group(columns: Seq[String], aggs: Seq[Agg], sorts: Seq[Sort])

case class Task(name: String,
                from: String,
                filter: Filter,
                group: Group,
                limit: Int
               )

