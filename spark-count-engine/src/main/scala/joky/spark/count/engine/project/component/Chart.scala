package joky.spark.count.engine.project.component

import java.util.Date

import joky.spark.count.engine.project.component.helper.{ComponetType, TimeUnit}


case class Chart(id: String,
                 name: String,
                 from: Table,
                 startDate: Date,
                 endDate: Date,
                 timeUnit: TimeUnit,
                 metrics: Seq[Metric],
                 filtres: Seq[Filter]) extends Component (id, name, ComponetType.CHART) {

}
