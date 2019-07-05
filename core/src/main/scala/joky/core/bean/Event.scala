package joky.core.bean

import java.sql.Timestamp

//sealed trait EventAction { val name: String }
//case object PAGEVIEW extends EventAction{ val name = "pageview"}
//case object ORDER extends EventAction{ val name = "pageview"}
//case object PAGEVIEW extends EventAction{ val name = "pageview"}
//case object PAGEVIEW extends EventAction{ val name = "pageview"}
//case object PAGEVIEW extends EventAction{ val name = "pageview"}

object EventAction extends Enumeration {
    type EventAction = Value
    val pageview, order, pay, click, login, logout, trackPageStart, pageEnd, search,
    trackRegister, addCart, deleteOrder, payment, openChannel, receiveJPush = Value
}

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/25 19:05
  * @Description:
  */
case class Event(
                    eventTime: Long,
                    eventName: String,
                    siteId: String,
                    sessionId: String,
                    seStartTime: Long,
                    deviceId: String,
                    userId: String,
                    ip: String,
                    userAgent: String,
                    uaName: String,
                    uaMajor: String,
                    resolution: String,
                    language: String,
                    netType: String,
                    country: String,
                    plugin: String,
                    continentCode: String, // 洲代码
                    region: String, // 区域或省
                    city: String, // 城市
                    lat: Double, //经度
                    lgt: Double,
                    url: String,
                    title: String,
                    referrer: String,
                    eventBody: String = """ """.stripMargin
                )

class Session(val sessionId: String,
              val seStartTime: Long,
              var userId: String = null,
              var lastTime: Long)


object Event {
    val eventNames = List("pageview", "order", "pay", "click", "login", "logout")
    //    val sessionIds = Range(0, 1).map("s" + _).toList
    val sessionIds = Range(0, 10).map("s" + _).toList
    val userIds = Range(0, 5).map("u" + _).toList
}


