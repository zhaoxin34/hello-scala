package joky.spark.de.entity


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