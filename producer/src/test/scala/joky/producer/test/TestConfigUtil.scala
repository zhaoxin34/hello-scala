package joky.producer.test

import joky.producer.util.ConfigUtil
import org.scalatest.FlatSpec
case class TestObj(username: String, gender: Boolean, parent: Seq[String])

class TestConfigUtil extends FlatSpec {
    "ConfigUtil" should "readYamlFile from file noEmpty" taggedAs producer in {
        val test = ConfigUtil.readYamlFile("producer/src/test/resources/test.yaml", classOf[TestObj])
        assert(test.parent.nonEmpty && test.username.nonEmpty)
    }

    it should "readFile noEmpty" taggedAs producer in {
        assert(ConfigUtil.readFile("producer/src/test/resources/test.yaml").nonEmpty)
    }
}
