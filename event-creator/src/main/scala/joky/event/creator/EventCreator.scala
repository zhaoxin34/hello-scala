package joky.event.creator

import joky.core.bean.Event
import joky.event.creator.impl.EventProducer

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._


/**
  * 事件生成器
  * 可以批量生产事件或者通过消费器消费生产的事件
  */
object EventCreator {

    def createEventList(visitorPoolSize: Int = 10, userIdPerVisitor: Int = 3, eventCreateCountPerSecond: Int = 10,
                        timing: Long = System.currentTimeMillis(), duration: Duration = 10 seconds, visitorCount: Int = 10): Seq[Event] = {
        val ep = EventProducer.createEventProducer(visitorPoolSize, userIdPerVisitor, eventCreateCountPerSecond)
        ep match {

            case Some(x) =>
                val eventConsumer = new ToEventSeqConsumer()
                // 消费
                x.consumeEvent(timing, duration, visitorCount, eventConsumer)
                // 返回
                eventConsumer.eventSeq

            case None => Seq.empty
        }
    }
}
