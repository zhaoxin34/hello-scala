package joky.spark.count.engine.project.component

import joky.spark.count.engine.project.component.helper.{ComponetType, DimensionType}

case class Dimension (id: String,
                      name: String,
                      dimensionType: DimensionType,
                      columns: Seq[Column],
                      filters: Seq[Filter]
                     ) extends Component (id, name, ComponetType.DIMENSION)
{

}
