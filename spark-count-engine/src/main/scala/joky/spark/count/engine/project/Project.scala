package joky.spark.count.engine.project

import joky.spark.count.engine.project.config._
import joky.spark.count.engine.project.component._
import joky.spark.count.engine.project.component.helper._

import scala.collection.mutable

class Project private(buildContext: BuildProjectContext) {
    def getChart(id: String): Chart = {
        buildContext.getComponent(id, ComponetType.CHART)
    }
}

class BuildProjectContext {
    private val product = mutable.HashMap[String, Component]()
    type Stuff = mutable.HashMap[String, Config]
    private val stuffMap = new Stuff()

    def addStuff(config: Config): BuildProjectContext = {
        stuffMap + (config.getId + config.componentType -> config)
        this
    }

    def addStuffs(configs: Seq[Config]): BuildProjectContext = {
        stuffMap ++ configs.map(config => config.getId + config.componentType -> config)
        this
    }

//    private def _buildCircle(unusedStuffs: Stuff) = {
//        val list = unusedStuffs.values.toSeq
//        list.foreach {
//            case a: ColumnConfig =>
//                product + (a.getId -> Column(a.getId, a.getName, ColumnType.valueOf(a.columnType)))
//            case _: TableConfig => {
//
//            }
//        }
//    }

    def build: BuildProjectContext = {
        this
    }

    def getComponents[T <: Component](componentType: ComponetType): Seq[T] = {
        product.filter(_._2.getType == componentType).values.toSeq.asInstanceOf
    }

    def getComponent[T <: Component] (id: String, componetType: ComponetType): T = {
        product.get(id) match {
            case Some(x) => x.asInstanceOf
//            case None =>
        }
    }

//    private def createComponent[T <: Component](id: String, componetType: ComponetType): T = {
//        val stuff: Config = stuffMap(id)
//        if (stuff == null)
//            throw new RuntimeException("config not found")
////        componetType match {
//////            case ComponetType.TABLE => Table(id, )
////        }
//    }
}

object Project {
    def buildProject(projectConfig: ProjectConfig): Project = {
        val context = new BuildProjectContext()

        context.addStuffs(projectConfig.tables)
            .addStuffs(projectConfig.events)
            .addStuffs(projectConfig.filters)
            .addStuffs(projectConfig.metrics)
            .addStuffs(projectConfig.dimensions)
            .addStuffs(projectConfig.charts)
            .build
        new Project(context)
    }
}
