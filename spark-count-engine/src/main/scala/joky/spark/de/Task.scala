package joky.spark.de

import joky.spark.de.entity.ValueColumn
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isEmpty
case class ValidResult(success: Boolean = true, message: String = null)

trait TaskComponent {
    def valid:ValidResult = ValidResult()
}

object FilterConnector extends Enumeration {
    val AND, OR = Value
}

trait Filter {
    def toCondition: String
    def connect(connector: FilterConnector.Value, filter: Filter): BiConnectedFilter = {
        BiConnectedFilter(connector, this, filter)
    }

    def and(filter: Filter): BiConnectedFilter = {
        connect(FilterConnector.AND, filter)
    }

    def or(filter: Filter): BiConnectedFilter = {
        connect(FilterConnector.OR, filter)
    }
}

case class EmptyFilter() extends Filter with TaskComponent {
    override def toCondition: String = {
        ""
    }
}

case class SimpleFilter(condition: String) extends Filter with TaskComponent {
    override def toCondition: String = {
        condition
    }
}

/**
  * 由左右组成二叉树结构的过滤器
  * @param connector
  * @param left
  * @param right
  */
case class BiConnectedFilter(connector: FilterConnector.Value, left: Filter, right: Filter) extends Filter with TaskComponent {
    override def toCondition: String = {
        this match {
                // 全空
            case BiConnectedFilter(null, null, null) | BiConnectedFilter(null, _: EmptyFilter, _: EmptyFilter) => ""

                // 右侧为空
            case BiConnectedFilter(null, l, null) => l.toCondition
            case BiConnectedFilter(_, l, _: EmptyFilter) => l.toCondition

                // 左侧为空
            case BiConnectedFilter(null, null, r) => r.toCondition
            case BiConnectedFilter(_, _: EmptyFilter, r) => r.toCondition

                // 全不空
            case BiConnectedFilter(c, l, r) => s"$l $c $r"
        }
    }
}

/**
  * 由一组过滤器和链接符组成的过滤器
  * @param connector
  * @param filters
  */
case class SeqConnectedFilter(connector: FilterConnector.Value, filters: Seq[Filter]) extends Filter with TaskComponent {
    override def toCondition: String = {
        this match {
            case SeqConnectedFilter(_, null) | SeqConnectedFilter(_, Nil) => ""
            case SeqConnectedFilter(cnt, flts) => flts.map(_.toCondition).filter(_.nonEmpty).reduceOption((a, b) => s"$a $cnt $b").getOrElse("")
        }
    }
}

/**
  * 加（）的组过滤器
  * @param filter
  */
case class GroupedFilter(filter: Filter) extends Filter with TaskComponent {
    override def toCondition: String = {
        filter match {
            case f: SimpleFilter => f.toCondition
            case f: EmptyFilter => f.toCondition
            case f: BiConnectedFilter => s"(${f.toCondition})"
            case f: SeqConnectedFilter => s"(${f.toCondition})"
            case f: GroupedFilter => f.toCondition
        }
    }
}

object SortOrder extends Enumeration {
    val ASC, DESC = Value
}


/**
  * 简单的聚合操作
  *
  * @param column
  * @param function
  * @param as
  */
case class Agg(column: String, function: String, as: String, order: Option[SortOrder.Value] = None) extends TaskComponent {
    override def valid: ValidResult = {
        if (isEmpty(column))
            ValidResult(false, "column is empty")

        else if (isEmpty(function))
            ValidResult(false, "function is empty")

        ValidResult()
    }

    override def toString: String = {
        s"Agg[$column, $function, $as ${order.map("," + _.toString)}]"
    }
}

case class Sort(column: String, order: String) {
    def isNone: Boolean = {
        if (isEmpty(column) && isEmpty(order)) {
            true
        } else false
    }

    override def toString: String = {
        s"$column $order"
    }
}

class Task extends TaskComponent

case class FromTask(from: String, limit: Int = 0, sorts: Seq[Sort] = Seq()) extends Task {
    override def valid: ValidResult = {
        if (StringUtils.isEmpty(from))
            ValidResult(false, "from is empty")
        else
            ValidResult()
    }

    override def toString: String = {
        s"From[$from limit $limit order by ${sorts.map(_.toString).mkString(",")}]"
    }
}

case class FilterTask(filters: Seq[Filter]) extends Task {
    override def toString: String = {
        s"Filter[${filters.map(_.toCondition).mkString(",")}]"
    }
}

case class AddValueColumnTask(valueColumns: Seq[ValueColumn]) extends Task {
    override def toString: String = {
        s"AddValueColumn[${valueColumns.map(_.toString).mkString(",")}]"
    }
}

case class AggsTask(aggs: Seq[Agg]) extends Task {
    override def valid: ValidResult = {
        aggs.map(_.valid).filter(!_.success).reduceOption((a, b) => ValidResult(a.success && b.success, a.message + "\n" + b.message)).getOrElse(ValidResult())
    }

    override def toString: String = {
        s"Agg[${aggs.map(_.toString).mkString(",")}]"
    }
}


case class DimensionAggsTask(dimensionColumns: Seq[String], aggs: Seq[Agg], limit: Int ) extends Task {
    override def valid: ValidResult = {
        aggs.map(_.valid).filter(!_.success).reduceOption((a, b) => ValidResult(a.success && b.success, a.message + "\n" + b.message)).getOrElse(ValidResult())
    }

    override def toString: String = {
        s"DimensionAggs[columns(${dimensionColumns.mkString(",")}), aggs(${aggs.map(_.toString).mkString(",")}), limit $limit]"
    }
}


/**
  * 顺序执行的任务
  * @param tasks 顺序任务
  */
case class SeqTask(tasks: Task*) extends Task {
    override def toString: String = {
        s"${tasks.map(_.toString).mkString("->")}"
    }
}

/**
  * 伞状任务，从task出发，每个task接着执行一个umbrellaTask，最后把所有task union在一起
  * @param task
  * @param umbrellaTasks
  */
case class UmbrellaUnionTask(task: Task, umbrellaTasks: Seq[Task]) extends Task {
    override def toString: String = {
        s"Umbrella[\n${umbrellaTasks.map(_.toString).map("\t" + task.toString + "->" + _).mkString("\n")}]"
    }
}


