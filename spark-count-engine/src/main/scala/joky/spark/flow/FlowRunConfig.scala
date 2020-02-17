package joky.spark.flow

import scala.concurrent.duration.TimeUnit

/**
  * @Auther: zhaoxin
  * @Date: 2020/2/14 23:39
  * @Description:
  */
case class FlowRunConfig(timeWindow: Int, timeWindowUnit: TimeUnit, runTimes: Int = -1) {

}
