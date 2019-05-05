package joky.spark.de.entity

import joky.spark.de.entity.helper.OrderType
import org.apache.commons.lang3.StringUtils.isEmpty


/**
  * 简单的聚合操作
  *
  * @param column
  * @param function
  * @param as
  */
case class Agg(column: String, function: String, as: String, order: Option[OrderType] = None) extends BaseEntity {

    override def valid: ValidResult = {
        this match {
            case Agg(_, _, _, _) if isEmpty(column) || isEmpty(function) || isEmpty(as) => ValidResult(false, s"Agg's column|function|as cannot not empty $this")
            case _ => ValidResult()
        }
    }

    override def toString: String = {
        s"Agg[$column, $function, $as ${order.map("," + _.toString)}]"
    }
}
