package joky.producer

import akka.actor.{Actor, ActorSystem, Props}
import org.apache.logging.log4j.scala.Logging

/**
  * @Auther: zhaoxin
  * @Date: 2019/1/10 20:05
  * @Description:
  */
object ProducerScheduler extends Logging {

    trait EventProducerMessage
    case class CreateEventMessage() extends EventProducerMessage


    class EventProducerActor(val createEventCallback: () => Unit) extends Actor {
        override def receive: Receive = {
            case CreateEventMessage => createEventCallback()
            case _ => logger.info("EventProducerActor receive unkonw message!")
        }
    }

    def startSchedule(callback: () => Unit): Unit = {
        import scala.concurrent.duration._
        val system = ActorSystem()
        val eventProducerActor = system.actorOf(Props(new EventProducerActor(callback)), name = "test")
        // Use system's dispatcher as ExecutionContext
        import system.dispatcher
        system.scheduler.schedule(
            0 seconds,
            60 seconds,
            eventProducerActor,
            CreateEventMessage())
    }
}
