package joky.sparkUdf

import java.util

import org.scalatest.FlatSpec

import scala.collection.JavaConversions._
import scala.util.Random

class TestFunnelALg  extends FlatSpec {

    val stepMax = 9
    val cleanFunnelData = true

    implicit def convertFunnelObject(scalaFunnelObj: Seq[(Int, Long)]): util.ArrayList[util.ArrayList[Object]] = {
        val ret = new util.ArrayList[util.ArrayList[Object]]()
        scalaFunnelObj.foreach(funnel => {
            val row = new util.ArrayList[Object]()
            row.add(funnel._1.asInstanceOf[Object])
            row.add(funnel._2.asInstanceOf[Object])
            ret.add(row)
        })
        ret
    }

    "FunnelAlg" should "return execlty result" in {
        val convertTime = 100

        // side test
        var funnelObjSide = Seq(
            (0, 100L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 0)

        // side test, 1,2两步同时发生，不记成功
        funnelObjSide = Seq(
            (0, 100L),
            (1, 100L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 0)

        funnelObjSide = Seq(
            (0, 100L),
            (1, 101L),
            (2, 102L),
            (3, 103L),
            (4, 104L),
            (5, 105L),
            (6, 106L),
            (7, 107L),
            (8, 108L),
            (9, 109L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 9)

        funnelObjSide = Seq(
            (2, 102L),
            (0, 100L),
            (1, 101L),
            (3, 103L),
            (7, 107L),
            (9, 109L),
            (5, 105L),
            (6, 106L),
            (4, 104L),
            (8, 108L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 9)

        funnelObjSide = Seq(
            (0, 100L),
            (1, 101L),
            (2, 102L),
            (3, 103L),
            (4, 104L),
            (5, 105L),
            (6, 106L),
            (7, 107L),
            (8, 108L),
            (9, 109L),
            (10, 110L),
            (11, 111L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 9)

        // 中间断掉的情况
        funnelObjSide = Seq(
            (0, 100L),
            (1, 101L),
            (2, 102L),
            (3, 103L),
            (6, 106L),
            (7, 107L),
            (8, 108L),
            (9, 109L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 3)

        funnelObjSide = Seq(
            (0, 100L),
            (1, 101L),
            (2, 102L),
            (3, 103L),

            (0, 200L),
            (1, 201L),
            (2, 202L),
            (3, 203L),
            (4, 204L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 4)

        funnelObjSide = Seq(
            (0, 100L),
            (1, 101L),
            (2, 102L),
            (3, 103L),

            (0, 200L),
            (1, 201L),
            (3, 203L),
            (4, 204L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 3)

        funnelObjSide = Seq(
            (0, 100L),
            (1, 101L),
            (2, 102L),
            (3, 103L),

            (0, 200L),
            (1, 201L),
            (3, 203L),
            (4, 204L)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, 200, cleanFunnelData) == 4)
    }

    def performTest(stepCount: Int): Long = {
        val convertTime = 100000L
        val funnelObj = (0 until stepCount).map(i => (Random.nextInt(stepMax + 1), (Random.nextDouble() * convertTime).asInstanceOf[Long])).toSeq
        val begin = System.currentTimeMillis()
        val funnelRet = FunnelAlg.countFunnel(funnelObj, stepMax, convertTime, true)
        println("funnelRet = " + funnelRet)
        val cost = System.currentTimeMillis() - begin
        println(s"$stepCount step cost time: $cost")
        cost
    }

    it should "has good performnence" in {
        (10 to 200 by 10).foreach(
            i => assert(performTest(i) < 60000)
        )
    }
}
