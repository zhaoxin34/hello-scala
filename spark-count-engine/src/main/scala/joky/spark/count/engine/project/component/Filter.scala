package joky.spark.count.engine.project.component

import joky.spark.count.engine.project.component.helper.ComponetType

case class Filter(id: String,
                  name: String,
                  value: String) extends Component (id, name, ComponetType.FILTER) {

}
