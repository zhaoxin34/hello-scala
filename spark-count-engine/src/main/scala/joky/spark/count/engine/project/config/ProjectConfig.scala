package joky.spark.count.engine.project.config

import com.fasterxml.jackson.annotation.JsonProperty
import joky.core.util.ConfigUtil

class Config private[config](id: String, name: String)

case class ColumnConfig(
                       id: String,
                       name: String,
                       columnType: String = "STRING") extends Config(id, name)

case class TableConfig(id: String,
                       name: String,
                       db: String,
                       timestampColumn: String,
                       dateColumn: String,
                       columns: Seq[ColumnConfig]
                      )  extends Config(id, name)

case class EventConfig(id: String,
                       name: String,
                       filter: String
                      ) extends Config(id, name)

case class SegmentConfig(id: String,
                         name: String,
                         filters: Seq[String]
                        ) extends Config(id, name)

case class AggConfig(column: String,
                     function: String)

case class MetricsConfig(id: String,
                         name: String,
                         agg: AggConfig,
                         segments: Seq[String],
                         filters: Seq[String]
                        ) extends Config(id, name)

case class DimensionConfig(id: String,
                           name: String,
                           dimensionType: String,
                           columns: Seq[String],
                           filters: Seq[String]
                          ) extends Config(id, name)

case class ChartConfig(id: String,
                       name: String,
                       plan: String,
                       from: String,
                       period: Seq[String],
                       metrics: Seq[String],
                       filters: Seq[String]
                      ) extends Config(id, name)

case class ProjectConfig(id: String,
                         name: String,
                         tables: Seq[TableConfig],
                         events: Seq[EventConfig],
                         segments: Seq[SegmentConfig],
                         metrics: Seq[MetricsConfig],
                         dimensions: Seq[DimensionConfig],
                         charts: Seq[ChartConfig]
                        ) extends Config(id, name)

object ProjectConfig {
    def buildConfig(yamlFilePath: String): ProjectConfig = {
        ConfigUtil.readYamlFile(yamlFilePath, classOf[ProjectConfig])
    }
}
