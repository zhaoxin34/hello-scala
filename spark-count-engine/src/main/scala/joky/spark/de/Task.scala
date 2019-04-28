package joky.spark.de

import com.fasterxml.jackson.module.scala.JsonScalaEnumeration
import joky.spark.de
import org.apache.commons.lang3.StringUtils.isEmpty
import org.codehaus.jackson.`type`.TypeReference
import org.codehaus.jackson.annotate.{JsonProperty, JsonSubTypes, JsonTypeInfo}
import org.codehaus.jackson.annotate.JsonSubTypes.Type
case class ValidResult(success: Boolean = true, message: String = null)

trait TaskComponent {
    def valid:ValidResult = ValidResult()
}

object FilterConnector extends Enumeration {
    val AND, OR = Value
}

case class Filter(connector: FilterConnector.Value, conditions: Seq[String]) extends TaskComponent {
    def isNone: Boolean = {
        conditions match {
            case Seq(_) => false
            case _ => true
        }
    }

    def toCondition: String = {
        if (!isNone) {
            conditions.mkString(s" $connector ")
        } else {
            ""
        }
    }
}

object Filter {
    def simpleFilter(filter: String): Filter = {
        Filter(null, Seq(filter))
    }
}

case class Agg(column: String, function: String, as: String) extends TaskComponent {
    override def valid: ValidResult = {
        if (isEmpty(column))
            ValidResult(false, "column is empty")

        else if (isEmpty(function))
            ValidResult(false, "function is empty")

        ValidResult()
    }
}

case class Sort(column: String, order: String) {
    def isNone: Boolean = {
        if (isEmpty(column) && isEmpty(order)) {
            true
        } else false
    }
}

case class Group(columns: Seq[String], aggs: Seq[Agg], sorts: Seq[Sort]) extends TaskComponent {
    override def valid: ValidResult = {
        if (aggs == null || aggs.isEmpty)
            ValidResult(false, "aggs is empty")
        else {
            aggs.map(_.valid).filter(!_.success).reduceOption((a, b) => ValidResult(a.success && b.success, a.message + "\n" + b.message)).getOrElse(ValidResult())
        }
    }
}

case class Task(name: String,
                from: String,
                filter: Filter,
                group: Group,
                limit: Int = 0
               ) extends TaskComponent {

    override def valid: ValidResult = {
        if (isEmpty(from))
            ValidResult(false, "from is empty")

        val groupValidResult = group.valid
        if (!groupValidResult.success)
            groupValidResult

        else
            ValidResult()
    }
}


case class UnionTask(taskList: Seq[Task])