package joky.spark.count.engine.project.component

import joky.spark.count.engine.project.component.helper.ComponetType

class Component(id: String, name: String, componentType: ComponetType) {
    def getId: String  = id
    def getName: String = name
    def getType: ComponetType = componentType
}
