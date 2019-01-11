package joky.producer

import java.sql.Timestamp

import joky.core.bean.{Event, EventAction, Session}
import joky.core.util.SomeUtil
import joky.producer.site._
import org.apache.logging.log4j.scala.Logging

import scala.util.Random


class Device(
                val deviceId: String,
                val ip: String,
                val uaName: String,
                val uaMajor: String,
                val resolution: String) {

    private var title: String = _
    private var url: String = _
    private var siteId: String = _
    private var referrer: String = _
    private var session: Session = newSession
    var deviceTime: Long = 0

    val SESSION_EXPIRE_TIME = 30 * 60 * 1000

    def isSessionExpire: Boolean = {
        System.currentTimeMillis() - session.lastTime.getTime > SESSION_EXPIRE_TIME
    }

    def getSession: Session = {
        if (isSessionExpire)
            return newSession
        session
    }

    def newSession: Session = {
        val sessionId = SomeUtil.shortMd5(s"${ip}_${uaName}_${uaMajor}_${resolution}_$deviceTime")
        new Session(sessionId, new Timestamp(deviceTime), null, new Timestamp(deviceTime))
    }

    def pageview(siteId: String, title: String, url: String): Option[Event] = {
        this.referrer = this.url
        this.title = title
        this.url = url
        this.siteId = siteId
        action(EventAction.pageview)
    }

    def login(userId: String): Option[Event] = {
        action(EventAction.login, userId)
    }

    def order: Option[Event] = {
        action(EventAction.order)
    }

    def pay: Option[Event] = {
        action(EventAction.pay)
    }

    def logout: Option[Event] = {
        action(EventAction.logout)
    }

    def click: Option[Event] = {
        action(EventAction.click)
    }

    private def action(eventAction: EventAction.Value, userId: String = null): Option[Event] = {
        if (url == null || title == null || siteId == null)
            None

        session = getSession
        if (userId != null) {
            session.userId = userId
        }
        val eventTime = new Timestamp(deviceTime)
        val event = Event(eventTime, eventAction.toString, siteId, session.sessionId, session.seStartTime, deviceId, session.userId, url, title, referrer)
        // 登出
        if (eventAction.equals(EventAction.logout))
            session = newSession
        Some(event)
    }
}

/**
  * 访客类
  * @param device   设备
  * @param visitSite    他可以访问的网站
  * @param userIds  访客持有的用户id列表
  * @param eventCreateCountPerSecond   每个访客每分钟可以产生的事件数
  */
class Visitor(val device: Device, val visitSite: Site, val userIds: Seq[String], val eventCreateCountPerSecond: Int = 100) extends Logging{

    // 当前目录
    private var currentPageTree: Option[PageTree[Page]] = None
    private var currentPage: Option[Page] = None


    private def moveUntilFindPages(pageTree: PageTree[Page]): Option[PageTree[Page]] = {
        if (pageTree.subTree.isEmpty)
            return None

        var ret: Option[PageTree[Page]] = Option(pageTree)

        do {
            ret = SomeUtil.randomPick(ret.get.subTree)
        } while (ret.nonEmpty && ret.get.pageList.isEmpty)
        ret
    }

    private def move(): Unit = {
        val oldPageTree = currentPageTree

        if (currentPageTree.isEmpty) {
            currentPageTree = moveUntilFindPages(visitSite.pageTree)
        }
        else {
            if (currentPageTree.get.subTree.isEmpty) {
                currentPageTree = moveUntilFindPages(visitSite.pageTree)
            }
            else {
                currentPageTree = moveUntilFindPages(currentPageTree.get)
            }
        }
        logger.debug(s"Visitor[${device.deviceId}] Move from ${oldPageTree.map(_.value)} to ${currentPageTree.map(_.value)}")
    }

    /**
      * 在当前目录下，随便浏览个页面
      * @return
      */
    private def doPageView(): Option[Event] = {
        if (currentPageTree.isEmpty)
            return None

        currentPage = SomeUtil.randomPick(currentPageTree.get.pageList)
        if (currentPage.isEmpty)
            None
        else
            device.pageview(visitSite.siteId, currentPage.get.title, currentPage.get.url)
    }

    // 随便做点什么
    private def doSome(): Option[Event] = {

        if (currentPageTree.isEmpty)
            return None

        // 50%的概率或者当前目录下没有页面，先移动一下目录
        if (Random.nextDouble() <= 0.5 || currentPageTree.get.pageList.isEmpty) {
            move
        }

        // 50的几率原页不动
        if (Random.nextDouble() <= 0.5 && currentPage.nonEmpty) {
            doSomeAction(SomeUtil.randomPick(currentPage.get.actions).get)
        } else {
            doPageView
        }
    }

    private def doSomeAction(eventAction: EventAction.Value): Option[Event] = {
        logger.debug(s"Visitor[${device.deviceId}] will do action $eventAction")
        eventAction match {
            case EventAction.pageview => doPageView
            case EventAction.login => device.login(SomeUtil.randomPick(userIds).get)
            case EventAction.click => device.click
            case EventAction.logout => device.logout
            case EventAction.order => device.order
            case EventAction.pay => device.pay
            case _ => None
        }
    }

    /**
      * 执行一定时长的动作
      * @param timing 动作开始的时间戳
      * @param seconds 动作的时间，分钟
      * @return
      */
    def action(timing: Long = System.currentTimeMillis(), seconds: Int, eventConsumer: Event => Unit): Unit= {
        // 如果当前没有页面，先移动一下
        if (currentPageTree.isEmpty)
            move()

        // 如果移动后还没有页面，只能返回空
        if (currentPageTree.isEmpty)
            return

        val eventCount = seconds * eventCreateCountPerSecond

        logger.info(s"Visitor[${device.deviceId}] Create $seconds seconds Events, EventCounts maybe $eventCount")

        device.deviceTime = timing
        (0 until eventCount)
            .flatMap(_ => {
                device.deviceTime = device.deviceTime + 1
                doSome()
            })
            .foreach(eventConsumer(_))
    }
}

