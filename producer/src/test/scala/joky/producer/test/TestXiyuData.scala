package joky.producer.test

import scala.collection.JavaConversions._
import java.nio.file.{Files, Paths}
import java.util

import scala.collection.mutable


/**
  * @Auther: zhaoxin
  * @Date: 2019/2/13 15:55
  * @Description:
  */
object TestXiyuData extends App{
    val IP_INDEX = 2
    val OS_INDEX = 7
    val UA_INDEX = 9
    val S_JAVA_INDEX = 15
    val TIME_INDEX = 16
    val URL_INDEX = 18

    val lines: Seq[String] = Files.readAllLines(Paths.get("/Users/zhaoxin/Downloads/ehsy-ip明细/ehsy-pc站IP明细20190201-utf8.csv"))
    val dealList = new util.LinkedList[Array[String]]()
    lines.map(_.split(',')).reduce((a, b) => {
        b(URL_INDEX) = if (b(URL_INDEX) == null || b(URL_INDEX).isEmpty) a(URL_INDEX) else b(URL_INDEX)
        b(IP_INDEX) = if (b(IP_INDEX) == null || b(IP_INDEX).isEmpty) a(IP_INDEX) else b(IP_INDEX)
        b(OS_INDEX) = if (b(OS_INDEX) == null || b(OS_INDEX).isEmpty) a(OS_INDEX) else b(OS_INDEX)
        b(UA_INDEX) = if (b(UA_INDEX) == null || b(UA_INDEX).isEmpty) a(UA_INDEX) else b(UA_INDEX)
        b(TIME_INDEX) = if (b(TIME_INDEX) == null || b(TIME_INDEX).isEmpty) a(TIME_INDEX) else b(TIME_INDEX)
        b(S_JAVA_INDEX) = if (b(S_JAVA_INDEX) == null || b(S_JAVA_INDEX).isEmpty) a(S_JAVA_INDEX) else b(S_JAVA_INDEX)

        if (dealList.isEmpty)
            dealList.push(a)
        dealList.add(b)
        b
    })

    Files.write(Paths.get("/Users/zhaoxin/Downloads/ehsy-ip明细/dealed.csv"),
        dealList
                .map(arr => Array(arr(URL_INDEX), arr(IP_INDEX), arr(OS_INDEX), arr(UA_INDEX), arr(TIME_INDEX), arr(S_JAVA_INDEX)))
            .map(_.mkString(",")))

//    dealList
//        .filter(arr => arr(S_JAVA_INDEX) == "支持" && arr(UA_INDEX) == "IE 8.0")
//        .foreach(b => println(s"\t${b(IP_INDEX)}\t${b(URL_INDEX)}\t${b(OS_INDEX)}\t${b(UA_INDEX)}\t${b(S_JAVA_INDEX)}\t${b(TIME_INDEX)}"))
//
//    dealList.map(_(IP_INDEX)).distinct.foreach(println)
}
