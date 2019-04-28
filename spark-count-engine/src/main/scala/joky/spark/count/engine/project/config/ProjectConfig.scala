package joky.spark.count.engine.project.config

import joky.core.util.ConfigUtil
import joky.spark.count.engine.project.config.helper.ComponetType

class Config(id: String, name: String, val componentType: ComponetType) {
    def getId: String = id
    def getName: String = name
}

case class ColumnConfig(
                       id: String,
                       name: String,
                       columnType: String = "STRING") extends Config(id, name, ComponetType.COLUMN)

case class TableConfig(id: String,
                       name: String,
                       db: String,
                       timestampColumn: String,
                       dateColumn: String,
                       columns: Seq[ColumnConfig]
                      )  extends Config(id, name, ComponetType.TABLE)

case class EventConfig(id: String,
                       name: String,
                       filters: Seq[String]
                      ) extends Config(id, name, ComponetType.EVENT)

case class FilterConfig(id: String,
                         name: String,
                         values: Seq[String]
                        ) extends Config(id, name, ComponetType.FILTER)

case class MetricsConfig(id: String,
                         name: String,
                         column: String,
                         function: String,
                         filters: Seq[String]
                        ) extends Config(id, name, ComponetType.METRIC)

case class DimensionConfig(id: String,
                           name: String,
                           dimensionType: String,
                           columns: Seq[String],
                           filters: Seq[String]
                          ) extends Config(id, name, ComponetType.DIMENSION)

case class ChartConfig(id: String,
                       name: String,
                       plan: String,
                       from: String,
                       startDate: String,
                       endDate: String,
                       timeUnit: String,
                       metrics: Seq[String],
                       filters: Seq[String]
                      ) extends Config(id, name, ComponetType.CHART)

case class ProjectConfig(id: String,
                         name: String,
                         tables: Seq[TableConfig],
                         events: Seq[EventConfig],
                         filters: Seq[FilterConfig],
                         metrics: Seq[MetricsConfig],
                         dimensions: Seq[DimensionConfig],
                         charts: Seq[ChartConfig]
                        ) extends Config(id, name, ComponetType.PROJECT)

object ProjectConfig {
    def buildConfig(yamlFilePath: String): ProjectConfig = {
        ConfigUtil.readYamlFile(yamlFilePath, classOf[ProjectConfig])
    }
}
