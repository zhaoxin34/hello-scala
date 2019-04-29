package joky.spark.de.entity

import joky.spark.de.entity.helper.OrderType

case class Metric(label: String, aggFunction: String, aggColumn: Column, order: Option[OrderType] = None) {

}
