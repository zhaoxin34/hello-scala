package joky.spark.de.entity

import joky.spark.de.entity.helper.ColumnType

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/28 14:54
  * @Description:
  */
case class Column(table:Table, name: String, label: String, columnType: ColumnType = ColumnType.STRING) {

}
