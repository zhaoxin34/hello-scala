package joky.event.creator.creator

import com.fasterxml.jackson.core.`type`.TypeReference
import joky.core.bean.EventAction
import joky.core.util.ConfigUtil
import joky.event.creator.component.{Page, Site}
import joky.event.creator.creator.ComponentConfig.{PageConfig, SiteConfig}
import org.apache.logging.log4j.scala.Logging

/**
  * @Auther: zhaoxin
  * @Date: 2019/7/5 14:10
  * @Description:
  */
object SiteCreator extends Logging {
    val SITE_FILE = "event-creator/src/main/resources/data/site.yaml"
    val SITE_FILE_DATA_DIR = "event-creator/src/main/resources/data/site/"

    def createSiteList(siteFilePath: String = SITE_FILE, siteFileDataPath: String = SITE_FILE_DATA_DIR): Seq[Site] = {
        val siteConfigType = new TypeReference[List[SiteConfig]] {}
        val siteConfigList: Seq[SiteConfig] = ConfigUtil.readYamlFile(SITE_FILE, siteConfigType)
        logger.debug(s"Read site from file[$SITE_FILE], count=${siteConfigList.size} ")

        siteConfigList.map(siteConfig => {
            val pageLines = ConfigUtil.readFile(SITE_FILE_DATA_DIR + siteConfig.siteId)
            logger.debug(s"read page siteId=${siteConfig.siteId}, count=${pageLines.size}")

            val sitePages: Seq[Page] = pageLines
                .map(_.split(",")
                    .map(_.trim))
                .map(arr => Page(arr(0), arr(1), EventAction.values.toList))

            Site(siteConfig.name, siteConfig.siteId, sitePages)
        })
    }
}
