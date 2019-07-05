package joky.event.creator.test

import java.util.Date

import joky.event.creator.component.{FlatActivationCuv, NormalActivationCuv}
import org.apache.commons.math3.distribution.NormalDistribution
import org.scalatest.FlatSpec

/**
  * 测试正太分布输出
  *
  * @Auther: zhaoxin
  * @Date: 2019/7/4 14:41
  * @Description:
  */
class TestActivationCuv extends FlatSpec {
    "FlatActivationCuv" should "always output the input posssiblity" taggedAs test.eventCreator in {
        val flatActivationCuv = new FlatActivationCuv()
        val posibility = flatActivationCuv.getPosibilty(new Date(), 60)
        // 概率应该介乎于59秒和60秒
        assert(posibility > 59 / 86400 && posibility < 61 / 86400)
    }

    "NormalActivationCuv" should "possibility add up to 1" taggedAs test.eventCreator in {
        val normalCuv = new NormalActivationCuv(Set(8, 17))
        val timingBase = System.currentTimeMillis() / 86400000 * 86400000
        val sumPosibility =
            (0 to 1440)
                .map(i => normalCuv.getPosibilty(new Date(timingBase + i * 60000), 60))
                .sum
        assert(sumPosibility > 0.9 && sumPosibility < 1)
    }
}
