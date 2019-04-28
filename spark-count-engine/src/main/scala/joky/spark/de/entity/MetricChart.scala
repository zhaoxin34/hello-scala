package joky.spark.de.entity

import java.util.Date

import joky.spark.de.entity.helper.TimeUnit

case class MetricChart(name: String, tableMetrics: TableMetrics, startTime: Date, endTime: Date, timeUnit: TimeUnit = TimeUnit.DAY) {

}
