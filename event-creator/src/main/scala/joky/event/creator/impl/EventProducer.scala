package joky.event.creator.impl

import com.fasterxml.jackson.core.`type`.TypeReference
import joky.core.bean.EventAction
import joky.core.util.{ConfigUtil, SomeUtil}
import joky.event.creator.EventConsumer
import joky.event.creator.EventCreatorConfig.{DeviceConfig, PageConfig, SiteConfig}
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration

/**
  * 创建事件生成器
  *
  * @param visitorPoolSize 访客池的数量
  * @param userIdPerVisitor 每个访客最高可以拥有userId个数
  */
case class EventProducer private (visitorPoolSize: Int, userIdPerVisitor: Int, eventCreateCountPerSecond: Int, deviceList: Seq[DeviceConfig],
                                  ipList: Seq[String], siteList: Seq[SiteConfig], siteIdPageMap: Map[String, Seq[PageConfig]]) extends Logging {
    logger.info(s"create EventProducer, visitorPoolSize=$visitorPoolSize, " +
        s"userIdPerVisitor=$userIdPerVisitor, deviceList count=${deviceList.size}, ipList count=${ipList.size}" +
        s"siteList=$siteList, siteIdPageMap count=${siteIdPageMap.mapValues(v => v.size)}")
    logger.debug(s"ip samples:${ipList.take(3)}")
    logger.debug(s"device samples:${deviceList.take(3)}")
    logger.debug(s"site page samples:${siteIdPageMap.mapValues(_.take(3))}")

    private val visitorPool: ArrayBuffer[Visitor] = new ArrayBuffer(visitorPoolSize)

    private val userIdPool = (1 to visitorPoolSize * userIdPerVisitor).map(_ + "")

    /**
      * 创建page
      * @param configs
      * @return
      */
    private def createPage(configs: Seq[PageConfig]): Seq[Page] = {
        configs.map(pageConfig => new Page(pageConfig.url, pageConfig.title, EventAction.values.toList))
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
            SomeUtil.randomPickSome(userIds, userIdPerVisitor),
            eventCreateCountPerSecond
        )

        visitorPool += visitor
    }

    /**
      * 创建事件
      * @param timing 从哪个时间点开始创建
      * @param duration 创建多长时间的事件
      * @param visitorCount 使用多少个访客创建事件
      * @return
      */
    def consumeEvent(timing: Long, duration: Duration, visitorCount: Int, eventConsumer: EventConsumer): Unit= {
        val _visitorCount = Math.min(visitorCount, visitorPoolSize)
        if (_visitorCount < visitorCount)
            logger.warn(s"not enough visitor, visitorCount=$visitorCount, visitorPoolSize=$visitorPoolSize")

        for (i <- 0 until _visitorCount) {
            val visitor = SomeUtil.randomPick(visitorPool)
            if (visitor.nonEmpty) {
                logger.info(s"user deviceId[${visitor.get.device.deviceId}] create ${visitor.get.eventCreateCountPerSecond * duration.toSeconds} events")
                visitorPool(i).action(timing, duration.toSeconds, eventConsumer.consume)
            }
        }

        eventConsumer.close()
    }
}

object EventProducer extends Logging {

    val DEVICE_FILE = "event-creator/src/main/resources/data/device.data"
    val IP_FILE = "event-creator/src/main/resources/data/ip.data"
    val SITE_FILE = "event-creator/src/main/resources/data/site.yaml"
    val VISITOR_FILE = "event-creator/src/main/resources/data/visitor.yaml"
    val SITE_FILE_DIR = "event-creator/src/main/resources/data/site/"

    def createEventProducer(visitorPoolSize: Int = 10, userIdPerVisitor: Int = 3, eventCreateCountPerSecond: Int = 10): Option[EventProducer] = {
        logger.info(s"EventProducer Will Produce: visitorPoolSize=$visitorPoolSize, userIdPerVisitor=$userIdPerVisitor, eventCreateCountPerSecond=$eventCreateCountPerSecond")

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

            val producer = new EventProducer(visitorPoolSize, userIdPerVisitor, eventCreateCountPerSecond, deviceConfigList,
                ipList.map(_.trim), siteConfigList, pageConfigMap )
            Some(producer)
        }
        catch {
            case e: Exception =>
                logger.error(s"createEventProducer failed, case by: $e", e)
                None
        }
    }

}
