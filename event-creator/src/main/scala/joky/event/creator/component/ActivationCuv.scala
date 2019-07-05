package joky.event.creator.component

import java.util.Date

import org.apache.commons.math3.distribution.NormalDistribution

/**
  * 活跃度曲线, 输入一个时间，返回活跃的可能性
  */
trait ActivationCuv {
    val ONE_DAY_SECOND: Int = 24 * 60 * 60

    /**
      * 输入总量、时间、时长，返回活跃率
      * @param timing 数据产生的时间
      * @return 可能性，小于0的
      */
    def getPosibilty(timing: Date, seconds: Integer): Double

    /**
      * 获得这个时刻概率和平均概率的比值
      * @param timing
      * @param seconds
      * @return
      */
    def getPosibiltyPropotion(timing: Date, seconds: Integer): Double
}

/**
  * 平滑返回
  */
class FlatActivationCuv() extends ActivationCuv {
    override def getPosibilty(timing: Date, seconds: Integer): Double = {
       seconds.toDouble / ONE_DAY_SECOND
    }

    override def getPosibiltyPropotion(timing: Date, seconds: Integer): Double = {
        1
    }
}

/**
  * 正太分布活跃度
  * @param peekHours 峰值出现的时间，小时
  */
class NormalActivationCuv(peekHours:Set[Int]) extends ActivationCuv {
    // 过滤掉无效时间
    private val ph = peekHours.filter(h => h >=0 && h <24)
    private val noramls: Seq[NormalDistribution] = ph.map(h => new NormalDistribution(h.doubleValue(), 2.5)).toList

    override def getPosibilty(timing: Date, seconds: Integer): Double = {
        val zereTiming = timing.getTime / 1000 % 86400
        val start = zereTiming.toDouble / ONE_DAY_SECOND * 24
        val end = (zereTiming.toDouble + seconds) / ONE_DAY_SECOND * 24
        val p = noramls.map(n => n.probability(start, end)).sum / noramls.size
//        println(s"start=$start, end=$end, p=$p")
        p
    }

    override def getPosibiltyPropotion(timing: Date, seconds: Integer): Double = {
        getPosibilty(timing, seconds) / (seconds.toDouble / ONE_DAY_SECOND)
    }
}
