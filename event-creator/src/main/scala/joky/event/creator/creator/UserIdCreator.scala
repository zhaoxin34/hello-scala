package joky.event.creator.creator

import org.apache.logging.log4j.scala.Logging

/**
  * @Auther: zhaoxin
  * @Date: 2019/7/5 15:36
  * @Description:
  */
object UserIdCreator extends Logging {
    val MIN_USERID = 1000000

    def createUserIdList(count: Integer): Seq[String] = {
        Range(MIN_USERID, MIN_USERID + count).map(_.toString)
    }
}
