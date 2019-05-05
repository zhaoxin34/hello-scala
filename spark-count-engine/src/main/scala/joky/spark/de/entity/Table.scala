package joky.spark.de.entity

import org.apache.commons.lang3.StringUtils.isEmpty

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/28 14:54
  * @Description:
  */
case class Table(db:String, name: String, label: String) extends BaseEntity {

    def asteriskColumn: Column = TableColumn(this, "*", "*")

    def column(name: String, label: String): TableColumn = TableColumn(this, name, label)

    override def toString: String = s"$db.$name"

    override def valid: ValidResult = {
        if (isEmpty(db) || isEmpty(name))
            ValidResult(false, s"table is not valid table($db, $name)")
        else
            super.valid
    }
}
