package joky.event.creator.component

import java.sql.Timestamp

import joky.core.bean.{Event, EventAction}

/**
  * 访问设备，模拟浏览器或者智能设备
  */
case class Device(deviceId: String,
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
                  lgt: Double, // 维度)
                  mobileNo: String
                 ) {

}
