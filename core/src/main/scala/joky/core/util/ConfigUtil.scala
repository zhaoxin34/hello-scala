package joky.core.util

import java.io.{File, FileInputStream}
import java.nio.file.{Files, Paths}
import java.util.Properties

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import collection.JavaConverters._

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/28 14:29
  * @Description:
  */
object ConfigUtil {
    val mapper: ObjectMapper = new ObjectMapper(new YAMLFactory())
    mapper.registerModule(DefaultScalaModule)

    /**
      * 获得文件的绝对路径
      * @param path 路径，相对，或绝对
      * @return
      */
    private def getFileAbsolutePath(path: String): String = {
        if (path.startsWith("/"))
            return path
        System.getProperty("user.dir") + "/" + path
    }

    def readYaml[T](content: String, clazz: Class[T]) : T = {
        mapper.readValue(content, clazz)
    }

    def readYaml[T](file: File, clazz: Class[T]) : T = {
        mapper.readValue(file, clazz)
    }

    def readYaml[T](content: String, valueTypeRef: TypeReference[T]) : T = {
        mapper.readValue(content, valueTypeRef)
    }

    def readYaml[T](file: File, valueTypeRef: TypeReference[T]) : T = {
        mapper.readValue(file, valueTypeRef)
    }

    def readYamlFile[T](filePath: String, clazz: Class[T]) : T = {
        if (filePath.startsWith("/"))
            readYaml(new File(filePath), clazz)
        else
            mapper.readValue(new File(getFileAbsolutePath(filePath)), clazz)
    }

    def readYamlFile[T](filePath: String, valueTypeRef: TypeReference[T]) : T = {
        if (filePath.startsWith("/"))
            readYaml(new File(filePath), valueTypeRef)
        else
            mapper.readValue(new File(getFileAbsolutePath(filePath)), valueTypeRef)
    }

    def readFile(filePath: String): Seq[String] = {
        Files.readAllLines(Paths.get(filePath)).asScala
    }

    def readPorpertiesFile(filePath: String): Properties = {
        val props = new Properties()
        props.load(new FileInputStream(new File(getFileAbsolutePath(filePath))))
        props
    }
}
