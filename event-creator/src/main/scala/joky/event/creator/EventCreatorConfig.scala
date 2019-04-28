package joky.event.creator

object EventCreatorConfig {

    case class DeviceConfig(uaName: String, uaMajor: String, resolution: String)

    case class PageConfig(url: String, title: String)

    case class SiteConfig(name: String, siteId: String)
}