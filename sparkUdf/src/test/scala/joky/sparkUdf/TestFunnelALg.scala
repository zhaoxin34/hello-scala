package joky.sparkUdf

import java.util

import org.scalatest.FlatSpec

import scala.collection.JavaConversions._
import scala.util.Random

class TestFunnelALg  extends FlatSpec {

    val stepMax = 9
    val cleanFunnelData = true

    implicit def convertFunnelObject(scalaFunnelObj: Seq[(Int, Int)]): util.ArrayList[util.ArrayList[Object]] = {
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
        var funnelObjSide: Seq[(Int, Int)] = Seq(
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == -1)

        funnelObjSide = Seq(
            (0, 100)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 0)

        funnelObjSide = Seq(
            (1, 100)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == -1)

        // side test, 1,2两步同时发生，不记成功
        funnelObjSide = Seq(
            (0, 100),
            (1, 100)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 0)

        funnelObjSide = Seq(
            (0, 100),
            (1, 101),
            (2, 102),
            (3, 103),
            (4, 104),
            (5, 105),
            (6, 106),
            (7, 107),
            (8, 108),
            (9, 109)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 9)

        funnelObjSide = Seq(
            (2, 102),
            (0, 100),
            (1, 101),
            (3, 103),
            (7, 107),
            (9, 109),
            (5, 105),
            (6, 106),
            (4, 104),
            (8, 108)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 9)

        funnelObjSide = Seq(
            (0, 100),
            (1, 101),
            (2, 102),
            (3, 103),
            (4, 104),
            (5, 105),
            (6, 106),
            (7, 107),
            (8, 108),
            (9, 109),
            (10, 110),
            (11, 111)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 9)

        // 中间断掉的情况
        funnelObjSide = Seq(
            (0, 100),
            (1, 101),
            (2, 102),
            (3, 103),
            (6, 106),
            (7, 107),
            (8, 108),
            (9, 109)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 3)

        funnelObjSide = Seq(
            (0, 100),
            (1, 101),
            (2, 102),
            (3, 103),

            (0, 200),
            (1, 201),
            (2, 202),
            (3, 203),
            (4, 204)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 4)

        funnelObjSide = Seq(
            (0, 100),
            (1, 101),
            (2, 102),
            (3, 103),

            (0, 200),
            (1, 201),
            (3, 203),
            (4, 204)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, convertTime, cleanFunnelData) == 3)

        funnelObjSide = Seq(
            (0, 100),
            (1, 101),
            (2, 102),
            (3, 103),

            (0, 200),
            (1, 201),
            (3, 203),
            (4, 204)
        )
        assert(FunnelAlg.countFunnel(funnelObjSide, stepMax, 200, cleanFunnelData) == 4)
    }

    def performTest(stepCount: Int): Long = {
        val convertTime = 100000
        val funnelObj = (0 until stepCount).map(i => (Random.nextInt(stepMax + 1), (Random.nextDouble() * convertTime).asInstanceOf[Int])).toSeq
        val begin = System.currentTimeMillis()
        val funnelRet = FunnelAlg.countFunnel(funnelObj, stepMax, convertTime, true)
        println("funnelRet = " + funnelRet)
        val cost = System.currentTimeMillis() - begin
        println(s"$stepCount step cost time: $cost")
        cost
    }

    it should "has good performnence" in {
        (10000 to 100000 by 10000).foreach(
            i => assert(performTest(i) < 1000)
        )
    }
}
