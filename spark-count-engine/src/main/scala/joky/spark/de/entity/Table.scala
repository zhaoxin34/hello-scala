package joky.spark.de.entity

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/28 14:54
  * @Description:
  */
case class Table(db:String, name: String, label: String) {
    def asteriskColumn: Column = TableColumn(this, "*", "*")
    def column(name: String, label: String): TableColumn = TableColumn(this, name, label)
}
