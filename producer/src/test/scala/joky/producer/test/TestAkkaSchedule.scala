package joky.producer.test

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.protobuf.TextFormat.Printer

import scala.concurrent.duration._
import org.scalatest.FlatSpec

/**
  * @Auther: zhaoxin
  * @Date: 2019/1/10 17:45
  * @Description:
  */
class TestAkkaSchedule extends FlatSpec {
    // 任务延迟1秒启动，每3秒执行一次
    "TestAkkaSchedule" should "test" in {
//        val system = ActorSystem()
//        val Tick = "tick"
//        var now = System.currentTimeMillis()
//        class TickActor extends Actor {
//            def receive = {
//                case Tick => {
//                    println(System.currentTimeMillis() - now)
//                    now = System.currentTimeMillis()
//                    println("get tick")
//                }
//                case _ => println("get unkonw")
//            }
//        }
//        val tickActor = system.actorOf(Props(new TickActor()), name = "test")
//        //Use system's dispatcher as ExecutionContext
//        import system.dispatcher
//
//        //This will schedule to send the Tick-message
//        //to the tickActor after 0ms repeating every 50ms
//        val cancellable =
//        system.scheduler.schedule(
//            1 seconds,
//            3 seconds,
//            tickActor,
//            Tick)
//
//        Thread.sleep(1000 * 30)
//        //This cancels further Ticks to be sent
//        cancellable.cancel()
    }

}
