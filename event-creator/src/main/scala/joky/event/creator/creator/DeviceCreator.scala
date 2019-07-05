package joky.event.creator.creator

import joky.core.util.ConfigUtil
import joky.event.creator.component.Device
import org.apache.logging.log4j.scala.Logging

/**
  * 设备生成器
  */
object DeviceCreator extends Logging {
    val DEVICE_FILE = "event-creator/src/main/resources/data/device.data"

    /**
      * 从配置文件创建设备列表
      *
      * @param configPath
      * @return
      */
    def createDeviceList(configPath: String = DEVICE_FILE): Seq[Device] = {
        val deviceLines = ConfigUtil.readFile(configPath)
        logger.debug(s"read from file[$configPath], rows=${deviceLines.size}")

        val deviceList: Seq[Device] = deviceLines
            .map(_.split('|').map(_.trim))
            .filter(_.length == 15)
            .map(params => {
                Device(params(0), params(1), params(2), params(3), params(4),
                    params(5), params(6), params(7), params(8), params(9),
                    params(10), params(11), params(12), params(13).toDouble, params(14).toDouble)
            })
        logger.debug(s"get device size=${deviceList.size}")
        deviceList
    }
}
