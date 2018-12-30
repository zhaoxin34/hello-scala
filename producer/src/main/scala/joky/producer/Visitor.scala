package joky.producer

import java.sql.Timestamp

import joky.core.util.{Event, EventAction, Session, SomeUtil}

import scala.collection.mutable.ArrayBuffer
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
        val now = System.currentTimeMillis()
        val sessionId = SomeUtil.md5(s"${ip}_${uaName}_${uaMajor}_${resolution}_$now")
        new Session(sessionId, new Timestamp(now), null, new Timestamp(now))
    }

    def pageview(siteId: String, title: String, url: String): Event = {
        this.referrer = this.url
        this.title = title
        this.url = url
        this.siteId = siteId
        action(EventAction.pageview)
    }

    def login(userId: String): Event = {
        action(EventAction.login, userId)
    }

    def order: Event = {
        action(EventAction.order)
    }

    def pay: Event = {
        action(EventAction.pay)
    }

    def logout: Event = {
        action(EventAction.logout)
    }

    def click: Event = {
        action(EventAction.click)
    }

    private def action(eventAction: EventAction.Value, userId: String = null): Event = {
        session = getSession
        if (userId != null) {
            session.userId = userId
        }
        val now = System.currentTimeMillis()
        val eventTime = new Timestamp(now)
        val event = Event(eventTime, eventAction.toString, siteId, session.sessionId, session.seStartTime, deviceId, session.userId, url, title, referrer)
        // 登出
        if (eventAction.equals(EventAction.logout))
            session = newSession
        event
    }
}

/**
  * 访客类
  * @param device   设备
  * @param visitSite    他可以访问的网站
  * @param userIds  访客持有的用户id列表
  * @param eventCreateCountPerMiniute   每个访客每分钟可以产生的事件数
  */
class Visitor(val device: Device, val visitSite: Site, val userIds: Seq[String], val eventCreateCountPerMiniute: Int = 100) {

    // 当前页
    private var currentPage: Page = null

    private def doPageView: Event = {
        if (currentPage == null) {
            currentPage = visitSite.pages.head
        } else {
            currentPage = SomeUtil.randomPick(currentPage.subPages).orElse(Option(currentPage)).get
        }
        device.pageview(visitSite.siteId, currentPage.title, currentPage.url)
    }

    // 随便做点什么
    private def doSome(): Event = {
        // 80的几率原页不动
        if (Random.nextDouble() <= 0.8) {
            doSomeAction(SomeUtil.randomPick(currentPage.actions).get)
        } else {
            doPageView
        }
    }

    private def doSomeAction(eventAction: EventAction.Value): Event = {
        eventAction match {
            case EventAction.pageview => doPageView
            case EventAction.login => device.login(SomeUtil.randomPick(userIds).get)
            case EventAction.click => device.click
            case EventAction.logout => device.logout
            case EventAction.order => device.order
            case EventAction.pay => device.pay
        }
    }

    /**
      * 执行一定时长的动作
      * @param minutes
      * @return
      */
    def action(minutes: Int): Seq[Event] = {
        var start = 0

        val events: ArrayBuffer[Event] = ArrayBuffer()

        if (currentPage == null) {
            events += doPageView
            start += 1
        }
        for (i <- start to eventCreateCountPerMiniute) {
            events += doSome()
        }
        events
    }
}

