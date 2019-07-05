package joky.event.creator.creator

import joky.core.util.SomeUtil
import joky.event.creator.component.Visitor
import org.apache.logging.log4j.scala.Logging

import scala.util.Random

/**
  * @Auther: zhaoxin
  * @Date: 2019/7/5 15:21
  * @Description:
  */
object VisitorCreator extends Logging {
    val GENDER_LIST = Seq("男", "女")
    val MIN_AGE = 18
    val MAX_AGE = 60

    def createVisitorList(count: Integer = 100): Seq[Visitor] = {
        Range(0, 100).map(_ => {
            Visitor(SomeUtil.randomPick(GENDER_LIST).get, MIN_AGE + new Random().nextInt(MAX_AGE - MIN_AGE))
        })
    }
}
