package joky.spark.count.engine.project.component

import joky.spark.count.engine.project.component.helper.ComponetType

case class Table(id: String,
                 name: String,
                 db: String,
                 timeStampColumn: Column,
                 dateColumn: Column,
                 columns: Seq[Column]
                ) extends Component (id, name, ComponetType.TABLE) {

}
