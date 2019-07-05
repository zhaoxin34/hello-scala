package joky.core.util

import java.io.PrintWriter
import java.net.ServerSocket

import scala.collection.mutable
import scala.util.Random
;

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/26 16:32
  * @Description:
  */
object SomeUtil extends App {
    def openSocketPrinter(port: Int, callback: PrintWriter => Unit): Unit = {
        new Thread(new Runnable {
            override def run(): Unit = {
                val socket = new ServerSocket(port).accept()
                callback(new PrintWriter(socket.getOutputStream))
            }
        }).start()
    }

    def randomPick[T](list: Seq[T]): Option[T] = {
        list match {
            case Nil => None
            case l : Seq[_] => Some(l(new Random().nextInt(l.size)))
            case _ =>  None
        }
    }

    def randomPickSome[T](list: Seq[T], count: Int): Seq[T] = {
        (0 to count).flatMap(_ => randomPick[T](list))
    }

    /**
      * 随机选出列表中的元素，带有index的返回, 注意，选出总数量未必能达到count，但是至少会有1个，只要list不空
      * @param list
      * @param count
      * @tparam T
      * @return
      */
    def randomPickSomeWithIndex[T](list: Seq[T], count: Int): Map[Int, T] = {
        val realCount = Math.min(list.size, count)
        (0 to realCount).map(_ => {
            val i = new Random().nextInt(list.size)
            i -> list(i)
        }).toMap
    }

    def md5(s: String): String = {
        import java.security.MessageDigest
        import java.math.BigInteger
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(s.getBytes)
        val bigInt = new BigInteger(1,digest)
        val hashedString = bigInt.toString(16)
        hashedString
//        MessageDigest.getInstance("MD5").digest(s.getBytes).mkString
    }

    def shortMd5(s: String): String = {
        md5(s).substring(0, 16)
    }

//    md5("Hello")
//
//    println(randomPick(List()))
//    println(randomPick(List("a", "bc", "de")))
//
//    openSocketPrinter(9999, p => {
//        Thread.sleep(10000L)
//    })
}
