package joky.event.creator.component

import java.sql.Timestamp
import java.util.Date

import joky.core.bean.{Event, EventAction}
import joky.core.util.SomeUtil
import org.apache.logging.log4j.scala.Logging

import scala.util.Random

/**
  * @Auther: zhaoxin
  * @Date: 2019/7/4 17:06
  * @Description:
  */
class VisitorSession(val vistor: Visitor,
                     val device: Device,
                     val site: Site,
                     val userIds: Seq[String] = Nil,
                     val sourceUrl: String = null,
                     val sessionStartTime: Date = new Date()
                   ) extends Logging{

    // 创造sessionId
    val sessionId: String = SomeUtil.shortMd5(s"${vistor.age}_${vistor.gender}_${device.ip}_${device.userAgent}_${site.siteId}_${System.currentTimeMillis()}")

    var url: String = sourceUrl //设备当前url, 首次赋值为内部url
    var title: String = _ // 设备当前页面title
    var referer: String = _ // 设备的referer
    var deviceTime: Date = _ // 设备当前时间
    var userId: String = _ // 会话当前用户id

    // 当前目录
    private var currentPageTree: Option[PageTree[Page]] = None
    // 当前页面
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

    /**
      * 移动到某个目录下
      */
    private def move(): Unit = {
        val oldPageTree = currentPageTree

        if (currentPageTree.isEmpty) {
            currentPageTree = moveUntilFindPages(site.pageTree)
        }
        else {
            if (currentPageTree.get.subTree.isEmpty) {
                currentPageTree = moveUntilFindPages(site.pageTree)
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
        else {
            referer = url
            url = currentPage.get.url
            title = currentPage.get.title
            pageview()
        }
    }


    private def doSomeAction(eventAction: EventAction.Value): Option[Event] = {
        logger.debug(s"Visitor[${device.deviceId}] will do action $eventAction")
        eventAction match {
            case EventAction.pageview => doPageView()
            case EventAction.login => login(SomeUtil.randomPick(userIds).get)
            case EventAction.click => click
            case EventAction.logout => logout
            case EventAction.order => order
            case EventAction.pay => pay
            case _ => None
        }
    }

    // 随便做点什么
    def doSome(deviceTime: Date = new Date()): Option[Event] = {
        this.deviceTime = deviceTime
        // 如果当前没有页面，先移动一下
        if (currentPageTree.isEmpty)
            move()

        // 如果移动后还没有页面，只能返回空
        if (currentPageTree.isEmpty)
            return None

        // 50%的概率或者当前目录下没有页面，先移动一下目录
        if (Random.nextDouble() <= 0.5 || currentPageTree.get.pageList.isEmpty) {
            move()
        }

        // 50的几率原页不动
        if (Random.nextDouble() <= 0.5 && currentPage.nonEmpty) {
            doSomeAction(SomeUtil.randomPick(currentPage.get.actions).get)
        } else {
            doPageView()
        }
    }

    private def pageview(): Option[Event] = {
        action(EventAction.pageview)
    }

    private def login(userId: String): Option[Event] = {
        this.userId = userId
        action(EventAction.login)
    }

    private def order: Option[Event] = {
        action(EventAction.order)
    }

    private def pay: Option[Event] = {
        action(EventAction.pay)
    }

    private def logout: Option[Event] = {
        action(EventAction.logout)
    }

    private def click: Option[Event] = {
        action(EventAction.click)
    }

    private def action(eventAction: EventAction.Value): Option[Event] = {
        if (url == null || title == null)
            None

        val eventTime = new Timestamp(deviceTime.getTime)
        val event = Event(
            eventTime = eventTime.getTime,
            eventName = eventAction.toString,
            siteId = site.siteId,
            sessionId=  sessionId,
            seStartTime= sessionStartTime.getTime,
            deviceId= device.deviceId,
            userId= userId,
            ip= device.ip,
            userAgent= device.userAgent,
            uaName= device.uaName,
            uaMajor= device.uaMajor,
            resolution= device.resolution,
            language= device.language,
            netType= device.netType,
            country= device.country,
            plugin= device.plugin,
            continentCode= device.continentCode, // 洲代码
            region= device.region, // 区域或省
            city= device.city, // 城市
            lat= device.lat, //经度
            lgt= device.lgt,
            url= url,
            title= title,
            referer= referer,
            eventBody="",
            mobileNo = device.mobileNo)

        Some(event)
    }
}
