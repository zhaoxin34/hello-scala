package joky.event.creator.impl

import java.sql.Timestamp

import joky.core.bean.{Event, EventAction, Session}
import joky.core.util.SomeUtil

/**
  * @Auther: zhaoxin
  * @Date: 2019/4/10 14:46
  * @Description:
  */

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

    val SESSION_EXPIRE_TIME: Int = 30 * 60 * 1000

    def isSessionExpire: Boolean = {
        System.currentTimeMillis() - session.lastTime > SESSION_EXPIRE_TIME
    }

    def getSession: Session = {
        if (isSessionExpire)
            return newSession
        session
    }

    def newSession: Session = {
        val sessionId = SomeUtil.shortMd5(s"${ip}_${uaName}_${uaMajor}_${resolution}_$deviceTime")
        new Session(sessionId, deviceTime, null, deviceTime)
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
        val event = Event(eventTime.getTime, eventAction.toString, siteId, session.sessionId, session.seStartTime, deviceId, session.userId, url, title, referrer)
        // 登出
        if (eventAction.equals(EventAction.logout))
            session = newSession
        Some(event)
    }
}
