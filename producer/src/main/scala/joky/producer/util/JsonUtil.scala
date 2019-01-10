package joky.producer.util

import java.io.StringWriter
import java.util.Date

import com.fasterxml.jackson.databind.{DeserializationContext, JsonNode, ObjectMapper, SerializerProvider}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.{JsonFactory, JsonGenerator}
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.node.IntNode
import java.io.IOException
import java.sql.Timestamp

import com.fasterxml.jackson.databind.deser.std.StdDeserializer

object JsonUtil {
    val mapper: ObjectMapper = new ObjectMapper(new JsonFactory())
    mapper.registerModule(DefaultScalaModule)


    class DateSerializer(clazz: Class[Date]) extends StdSerializer[Date](clazz) {
        @throws[IOException]
        @throws[JsonProcessingException]
        override def serialize(value: Date, jgen: JsonGenerator, provider: SerializerProvider): Unit = {
            jgen.writeNumber(value.getTime)
        }
    }

    class DateDeserializer(clazz: Class[Date]) extends StdDeserializer[Date](clazz) {


        @throws[IOException]
        @throws[JsonProcessingException]
        def deserialize(jp: JsonParser, ctxt: DeserializationContext): Date = {
            val ts: Long = jp.getCodec.readValue(jp, classOf[Long])
            new Timestamp(ts)
        }
    }

    val module = new SimpleModule
    module.addSerializer(classOf[Date], new DateSerializer(classOf[Date]))
    mapper.registerModule(module)

    def toJson(json: AnyRef): String = {
        val outer = new StringWriter()
        mapper.writeValue(outer, json)
        outer.toString
    }

    def fromJson[T](json: String, clazz: Class[T]): T = {
        mapper.readValue(json, clazz)
    }

}
