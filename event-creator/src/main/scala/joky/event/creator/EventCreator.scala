package joky.event.creator

import java.util.Date

import joky.core.util.SomeUtil
import joky.event.creator.component._
import joky.event.creator.consumer.EventConsumer
import joky.event.creator.creator.{DeviceCreator, SiteCreator, UserIdCreator, VisitorCreator}
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable

/**
  * 创建事件生成器
  *
  * @param visitorPoolSize 访客池的数量
  * @param oneDayEventCount 一天可以产生多少数据
  * @param activationCuv 活跃曲线
  */
case class EventCreator private (visitorPoolSize: Int, oneDayEventCount: Long, activationCuv: ActivationCuv = new FlatActivationCuv()) extends Logging {
    logger.info(s"create EventProducer, visitorPoolSize=$visitorPoolSize, oneDayEventCount=$oneDayEventCount")

    // 最大session时长
    private val MAX_SESSION_MINUTES = 30

    // 用户id的样本量系数, 每个visitor的userId数量
    private val userIdSample = 2

    private val visitorPool: Seq[Visitor] = VisitorCreator.createVisitorList(visitorPoolSize)
    private val userIdPool: Seq[String] = UserIdCreator.createUserIdList(visitorPoolSize * userIdSample)
    private val devicePool: Seq[Device] = DeviceCreator.createDeviceList()
    private val sitePool: Seq[Site] = SiteCreator.createSiteList()
    private val sourceUrlPool: Seq[String] = Seq("http://baidu.com", "http://google.com", "http://bing.com", "http://soso.com", "http://sogou.com", "http://yahoo.com", "http://taobao.com", "http://qq.com", "http://360.com", "http://facebook.com", "http://twitter.com", null)

    // Int 是visitorPool的index
    private val visitorSessionPool: mutable.Map[Int, VisitorSession] = new mutable.HashMap[Int, VisitorSession]()

    // 从活跃用户池中选出的概率, 也就是上一批用户中，有多大概率某个人还是活跃的，@TODO 这个值最好能变成配置的，目前先写死
    private val keepActivePercent: Double = 0.8

    // 活跃的session
    private var activeSessions: Seq[VisitorSession] = Seq()

    private def createSession(visitor: Visitor, timing: Date): VisitorSession = {
        new VisitorSession(
            visitor,
            SomeUtil.randomPick(devicePool).get,
            SomeUtil.randomPick(sitePool).get,
            SomeUtil.randomPickSome(userIdPool, userIdSample),
            SomeUtil.randomPick(sourceUrlPool).get,
            timing
        )
    }

    // 首先看池子里有吗，没有或者失效了就创建一个
    private def getOrCreateSession(visitor: Visitor, visitorIndex: Int, timing: Date): VisitorSession = {
        visitorSessionPool.get(visitorIndex) match {
            case Some(s: VisitorSession) => if (timing.getTime - s.deviceTime.getTime > MAX_SESSION_MINUTES * 60 * 1000) createSession(visitor, timing) else s
            case None => createSession(visitor, timing)
        }
    }

    // 产生活跃session
    private def produceActiveSession(visitorCount: Int, timing: Date): Unit = {

        activeSessions = SomeUtil.randomPickSome(activeSessions, Math.min((activeSessions.size * keepActivePercent).toInt, visitorCount))
        logger.info(s"first pick up active session size: ${activeSessions.size}, visitorCount=$visitorCount")

        // 如果够数了
        if (activeSessions.size > visitorCount)
            return

        // 如果不够，继续选
        val restNumber = visitorCount - activeSessions.size
        logger.info(s"rest active session size: $restNumber")

        // 活跃访客挑选
        val activeVisitors: Map[Int, Visitor] = SomeUtil.randomPickSomeWithIndex(visitorPool, restNumber)
        logger.debug(s"consumeEvent activeVisitors=${activeVisitors.size}")

        // 使用活跃访客创建活跃session
        activeSessions ++= activeVisitors.map(a => getOrCreateSession(a._2, a._1, timing)).toList
        logger.info(s"active session count=${activeSessions.size}")
    }

    /**
      * 创建事件
      * @param timing 从哪个时间点开始创建
      * @param durationSeconds 创建多长时间的事件
      * @return
      */
    def consumeEvent(timing: Date, durationSeconds: Int, eventConsumer: EventConsumer*): Unit= {
        // 计算最近需要生成的事件总量
        val eventCount = Math.max((activationCuv.getPosibilty(timing, durationSeconds) * oneDayEventCount).toInt, 1)
        logger.debug(s"consumeEvent eventCount=$eventCount")

        // 计算需要的访客数量
        val visitorCount = Math.max((activationCuv.getPosibiltyPropotion(timing, durationSeconds) * visitorPoolSize).toInt, 1)
        logger.debug(s"consumeEvent visitorCount=$visitorCount")

        // 产生活跃用户
        produceActiveSession(visitorCount, timing)

        // 创建并消费事件
        0 to eventCount foreach (_ => {
            val theSession = SomeUtil.randomPick(activeSessions).get
            logger.debug(s"the session $theSession")

            theSession.doSome(timing).foreach(e =>
                eventConsumer.foreach(_.consume(e))
            )
        })

        eventConsumer.foreach(_.close())
    }
}
