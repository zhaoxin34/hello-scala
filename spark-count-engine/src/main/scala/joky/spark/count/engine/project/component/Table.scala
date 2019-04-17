package joky.spark.count.engine.project.component

case class Table(id: String,
                 name: String,
                 db: String,
                 timeStampColumn: Column,
                 dateColumn: Column,
                 columns: Seq[Column]
                ) {

}
