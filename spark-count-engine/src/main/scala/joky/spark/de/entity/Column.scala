package joky.spark.de.entity

import joky.spark.de.entity.helper.ColumnType

abstract class Column(val name: String, val label: String, val columnType: ColumnType = ColumnType.STRING)

case class ValueColumn(override val name: String, override val label: String, override val columnType: ColumnType = ColumnType.STRING, value: AnyVal) extends Column(name, label, columnType) {
    override def toString: String = {
        s"ValueColumn[$name, $label, $value]"
    }
}

case class TableColumn(table:Table, override val name: String, override val label: String, override val columnType: ColumnType = ColumnType.STRING) extends Column(name, label, columnType)
