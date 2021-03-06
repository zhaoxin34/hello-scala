package joky.core.test

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Date

import joky.core.util.JsonUtil
import org.scalatest.FlatSpec

import scala.util.Try

case class Person(name: String, gender: Boolean, birthDate: Date, sqlDate: Timestamp)

class TestJsonUtil extends FlatSpec {
    val person = Person("Rock", gender = false, new SimpleDateFormat("yyyy-mm-dd").parse("2018-01-01"), new Timestamp(new SimpleDateFormat("yyyy-mm-dd").parse("2018-01-01").getTime))

    "JsonUtil" should "object to json" taggedAs test.core in {
        val json = JsonUtil.toJson(person)
//        println(json)
        assert(json.nonEmpty)
    }

    it should "json to object" in {
        val p: Person = JsonUtil.fromJson("{\"name\":\"Rock\",\"gender\":false,\"birthDate\":1514736060000,\"sqlDate\":1514736060000}", classOf[Person])
//        println(p)
        assert(p.name == "Rock")
    }

    it should "try from json return object" in {
        val p: Try[Person] = JsonUtil.tryFromJson("{\"name\":\"Rock\",\"gender\":false,\"birthDate\":1514736060000,\"sqlDate\":1514736060000}", classOf[Person])
        assert(p.isSuccess)
        assert(p.get.name == "Rock")
    }

    it should "try from json failed" in {
        val p: Try[Person] = JsonUtil.tryFromJson("{\"name\":\"Rock\"", classOf[Person])
        assert(p.isFailure)
    }

}
