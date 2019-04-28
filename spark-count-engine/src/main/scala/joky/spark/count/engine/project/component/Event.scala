package joky.spark.count.engine.project.component

import joky.spark.count.engine.project.component.helper.ComponetType

case class Event(id: String,
                 name: String,
                 filter: Filter) extends Component (id, name, ComponetType.EVENT) {

}
