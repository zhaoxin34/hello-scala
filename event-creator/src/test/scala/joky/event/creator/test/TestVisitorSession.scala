package joky.event.creator.test

import java.util.Date

import joky.core.bean.EventAction
import joky.core.util.SomeUtil
import joky.event.creator.component._
import org.scalatest.FlatSpec

/**
  * @Auther: zhaoxin
  * @Date: 2019/7/4 18:00
  * @Description:
  */
class TestVisitorSession extends FlatSpec {
    "VisitorSession" should "create some event" taggedAs test.eventCreator in {
        val referer = "http://www.google.com"
        val session = new VisitorSession(
            Visitor("女", 18),
            device = Device(
                SomeUtil.shortMd5("diviceId"),
                "192.168.0.100",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36",
                "chrome",
                "2.0",
                "1024*768",
                "zh_CN",
                "4g",
                "realtime:1",
                "28",
                "北京",
                "北京",
                "中国",
                12.9,
                23.4,
                "13012341234"
            ),
            Site("test", "1234567890123456", Seq(
                Page("http://baidu.com", "baidu", EventAction.values.toList),
                Page("http://baidu.com/a", "baidu a", EventAction.values.toList))),
            Seq("100", "101"),
            referer,
            new Date()
        )
        (1 to 100).foreach(i => {
            val event = session.doSome(new Date())
            println(event)
//            assert(event.nonEmpty)
            if (i == 1)
                assert(event.get.referer == referer)
        })
    }
}
