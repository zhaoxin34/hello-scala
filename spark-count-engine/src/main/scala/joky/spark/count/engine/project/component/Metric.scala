package joky.spark.count.engine.project.component

import joky.spark.count.engine.project.component.helper.ComponetType

case class Metric(id: String,
                  name: String,
                  column: Column,
                  filters: Seq[Filter]
                  ) extends Component (id, name, ComponetType.METRIC) {

}
