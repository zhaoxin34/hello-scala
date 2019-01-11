package joky.core.test

import joky.core.util.ConfigUtil
import org.scalatest.FlatSpec

case class TestObj(username: String, gender: Boolean, parent: Seq[String])

class TestConfigUtil extends FlatSpec {
    "ConfigUtil" should "readYamlFile from file noEmpty"  in {
        val test = ConfigUtil.readYamlFile("producer/src/test/resources/test.yaml", classOf[TestObj])
        assert(test.parent.nonEmpty && test.username.nonEmpty)
    }

    it should "readFile noEmpty" taggedAs test.core in {
        assert(ConfigUtil.readFile("producer/src/test/resources/test.yaml").nonEmpty)
    }
}
