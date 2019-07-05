package joky.event.creator.creator

/**
  * @Auther: zhaoxin
  * @Date: 2019/7/4 13:36
  * @Description:
  */
object ComponentConfig {

    @Deprecated
    case class DeviceConfig(uaName: String, uaMajor: String, resolution: String)

    @Deprecated
    case class PageConfig(url: String, title: String)

    case class SiteConfig(name: String, siteId: String)
}
