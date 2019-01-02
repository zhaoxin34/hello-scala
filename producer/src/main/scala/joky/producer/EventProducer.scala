package joky.producer

import com.fasterxml.jackson.core.`type`.TypeReference
import joky.core.util.{Event, SomeUtil}
import joky.producer.util.ConfigUtil
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable.ArrayBuffer
import joky.producer.site._

/**
  * 创建事件生成器
  *
  * @param visitorPoolSize 访客池的数量
  * @param userIdPerVisitor 每个访客最高可以拥有userId个数
  */
case class EventProducer private (visitorPoolSize: Int, userIdPerVisitor: Int, deviceList: Seq[DeviceConfig],
                                  ipList: Seq[String], siteList: Seq[SiteConfig], siteIdPageMap: Map[String, Seq[PageConfig]]) extends Logging {
    logger.info(s"create EventProducer, visitorPoolSize=$visitorPoolSize, " +
        s"userIdPerVisitor=$userIdPerVisitor, deviceList count=${deviceList.size}, ipList count=${ipList.size}" +
        s"siteList=$siteList, siteIdPageMap count=${siteIdPageMap.mapValues(v => v.size)}")
    logger.info(s"ip samples:${ipList.take(3)}")
    logger.info(s"device samples:${deviceList.take(3)}")
    logger.info(s"site page samples:${siteIdPageMap.mapValues(_.take(3))}")

    private val visitorPool: ArrayBuffer[Visitor] = new ArrayBuffer(visitorPoolSize)

    private val userIdPool = (1 to visitorPoolSize * userIdPerVisitor).map(_ + "")

    /**
      * 创建page
      * @param configs
      * @return
      */
    private def createPage(configs: Seq[PageConfig]): Seq[Page] = {
        Seq()
    }

    private val siteIdMap: Map[String, Site] = siteList.map(siteConfig => siteConfig.siteId -> {
        val sitePages: Seq[Page] = createPage(siteIdPageMap(siteConfig.siteId))
        Site(siteConfig.name, siteConfig.siteId, sitePages)
    }).toMap

    for (i <- 1 to visitorPoolSize) {
        val deviceConfig: DeviceConfig = SomeUtil.randomPick(deviceList).get
        val ip: String = SomeUtil.randomPick(ipList).get
        val siteId = SomeUtil.randomPick(siteList.map(_.siteId)).get
        val userIds: Seq[String] = SomeUtil.randomPickSome(userIdPool, userIdPerVisitor)

        val device: Device = new Device(
            SomeUtil.md5(i + "").substring(0, 16),
            ip, deviceConfig.uaName, deviceConfig.uaMajor, deviceConfig.resolution)

        val visitor = new Visitor(
            device,
            siteIdMap(siteId),
            SomeUtil.randomPickSome(userIds, userIdPerVisitor)
        )

        visitorPool += visitor
    }

    /**
      * 创建事件
      * @param minutes 创建多长时间的事件
      * @param visitorCount 使用多少个访客创建事件
      * @return
      */
    def createEvent(minutes: Int, visitorCount: Int): Seq[Event] = {
        val events = ArrayBuffer[Event]()
        val _visitorCount = Math.min(visitorCount, visitorPoolSize)
        if (_visitorCount < visitorCount)
            logger.warn(s"not enough visitor, visitorCount=$visitorCount, visitorPoolSize=$visitorPoolSize")

        for (i <- 0 to _visitorCount) {
            events ++= visitorPool(i).action(minutes)
        }
        events
    }

}

object EventProducer extends Logging {

    val DEVICE_FILE = "producer/src/main/resources/data/device.data"
    val IP_FILE = "producer/src/main/resources/data/ip.data"
    val SITE_FILE = "producer/src/main/resources/data/site.yaml"
    val VISITOR_FILE = "producer/src/main/resources/data/visitor.yaml"
    val SITE_FILE_DIR = "producer/src/main/resources/data/site/"

    def createEventProducer(visitorPoolSize: Int = 10, userIdPerVisitor: Int = 3): Option[EventProducer] = {
        logger.info(s"EventProducer Will Produce: visitorPoolSize=$visitorPoolSize, userIdPerVisitor=$userIdPerVisitor")

        try {
            val deviceList = ConfigUtil.readFile(DEVICE_FILE)
            logger.info(s"Read ${deviceList.size} device from file [$DEVICE_FILE]")

            val ipList= ConfigUtil.readFile(IP_FILE)
            logger.info(s"Read ${ipList.size} ip from file [$IP_FILE]")

            val siteConfigType = new TypeReference[List[SiteConfig]] {}
            val siteConfigList: Seq[SiteConfig]  = ConfigUtil.readYamlFile(SITE_FILE, siteConfigType)
            logger.info(s"Read ${siteConfigList.size} site from file[$SITE_FILE]")

            val deviceConfigList = deviceList
                // split and trim to array(
                .map(_.split(",").map(_.trim))
                .map(arr => DeviceConfig(arr(0), arr(1), arr(2)))

            val pageConfigMap: Map[String, Seq[PageConfig]] = siteConfigList
                .map(_.siteId)
                .map(siteId => siteId ->
                    ConfigUtil.readFile(SITE_FILE_DIR + siteId).map(_.split(",").map(_.trim)).map(arr => PageConfig(arr(0), arr(1))).toList
                ).toMap

            val producer = new EventProducer(visitorPoolSize, userIdPerVisitor, deviceConfigList,
                ipList.map(_.trim), siteConfigList, pageConfigMap )
            Some(producer)
        }
        catch {
            case e: Exception =>
                logger.error(s"createEventProducer failed, case by: $e", e)
                None
        }
    }

    //    private val visitorPool: Seq[Visitor] = Seq(
    //        new Visitor(
    //            new Device("f3eee499f96f1dac", "223.104.4.49", "chrome", "1.0", "1024*768"),
    //            Site.laiyifenSite,
    //            Seq(
    //                "2962814", "2962815", "2962816", "2962817"
    //            ),
    //            10
    //        ),
    //        new Visitor(
    //            new Device("f3eee499f96f1dab", "223.104.4.49", "firefox", "3.5", "1920*1200"),
    //            Site.laiyifenSite,
    //            Seq(
    //                "1962814", "1962815", "1962816", "1962817"
    //            ),
    //            5
    //        )
    //    )
    //
    //    def createEvent(minutes: Int, visitorCount: Int): Seq[Event] = {
    //        val events = ArrayBuffer[Event]()
    //        for (i <- 0 to visitorCount) {
    //            events ++= visitorPool(i).action(minutes)
    //        }
    //        events
    //    }
    //
    //    val events = createEvent(1, 1)
    //
    //    events.foreach(logger.warn(_))
}
