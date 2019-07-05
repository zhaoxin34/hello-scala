package joky.event.creator.test

import joky.event.creator.creator.DeviceCreator
import org.scalatest.FlatSpec

/**
  * @Auther: zhaoxin
  * @Date: 2019/7/5 15:15
  * @Description:
  */
class TestDeviceCreator extends FlatSpec {

    "DeviceCreator" should "create some device" taggedAs test.eventCreator in {
        assert(DeviceCreator.createDeviceList().nonEmpty)
    }

}
