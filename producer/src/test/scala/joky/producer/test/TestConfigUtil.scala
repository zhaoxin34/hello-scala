package joky.producer.test

import joky.producer.ConfigUtil
import joky.producer.ConfigUtil.readYamlFile
import org.junit.{Assert, Test}

case class TestObj(username: String, gender: Boolean, parent: Seq[String])

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/29 20:06
  * @Description:
  */
class TestConfigUtil {

    @Test
    def testReadYaml(): Unit = {
        val test = ConfigUtil.readYamlFile("producer/src/test/resources/test.yaml", classOf[TestObj])
        println(test)
        Assert.assertTrue(
            test.parent.nonEmpty && test.username.nonEmpty
        )
    }

    @Test
    def testReadFile(): Unit = {
        Assert.assertTrue(
            ConfigUtil.readFile("producer/src/test/resources/test.yaml").nonEmpty
        )

    }

}
