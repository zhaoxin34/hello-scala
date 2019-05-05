package joky.spark.de.entity

import org.apache.commons.lang3.StringUtils.isEmpty

case class Sort(column: String, order: String) {
    override def toString: String = {
        s"$column $order"
    }
}

