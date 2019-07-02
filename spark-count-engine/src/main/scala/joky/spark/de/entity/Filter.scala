package joky.spark.de.entity

import java.lang
import java.util.Date

import joky.spark.de.entity.helper.{ColumnType, OperatorType}
import org.apache.commons.lang3.StringUtils


object FilterConnector extends Enumeration {
    val AND, OR = Value
}


trait Filter extends BaseEntity {
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

    override def toString: String = toCondition
}

object Filter {
    private def formatValue(value: Any, operator: OperatorType, columnType: ColumnType): Any = {
        val newValue = columnType match {
            case ct if ct == ColumnType.INTEGER =>
                value match {
                    case v: String => v
                    case v: Int => v
                    case v: Long => v.toInt
                    case v: Integer => v
                    case v: lang.Long => v.toInt
                    case _ => throw new RuntimeException(s"$value cannot parse to int")
                }
            case ct if ct == ColumnType.LONG =>
                value match {
                    case v: String => v.toLong
                    case Int | Long | _:Integer | _:lang.Long => value
                    case d: Date => d.getTime
                    case _ => throw new RuntimeException(s"$value cannot parse to LONG")
                }
            case ct if ct == ColumnType.STRING => s"'${StringUtils.replace(value.toString, "'", "\\'")}'"
            case _ => value
        }

        operator match {
            case op if op == OperatorType.IN => s"($newValue)"
            case _ => newValue
        }
    }

    def createSimpleFilter(operand: Column, operator: OperatorType, value: Option[Any] = None): SimpleFilter = {
        if (operator.isNoValue) {
            SimpleFilter(s"${operand.name} ${operator.getExpress}")
        } else {
            SimpleFilter(s"${operand.name} ${operator.getExpress} ${formatValue(value.get, operator, operand.columnType)}")
        }
    }
}

case class EmptyFilter() extends Filter {
    override def toCondition: String = {
        ""
    }
}

case class SimpleFilter(condition: String) extends Filter {
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
case class BiConnectedFilter(connector: FilterConnector.Value, left: Filter, right: Filter) extends Filter {
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
case class SeqConnectedFilter(connector: FilterConnector.Value, filters: Seq[Filter]) extends Filter {
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
case class GroupedFilter(filter: Filter) extends Filter {
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