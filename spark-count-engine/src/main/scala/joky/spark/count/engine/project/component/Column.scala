package joky.spark.count.engine.project.component

import joky.spark.count.engine.project.component.helper.{ColumnType, ComponetType}

case class Column(id: String,
             name: String,
             columnType: ColumnType) extends Component (id, name, ComponetType.COLUMN) {

}
