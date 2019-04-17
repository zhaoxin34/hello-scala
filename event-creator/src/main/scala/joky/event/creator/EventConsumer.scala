package joky.event.creator

import joky.core.bean.Event

import scala.collection.mutable.ArrayBuffer

/**
  * 事件消费器,
  * @see joky.event.creator.impl.EventProducer.consumeEvent
  */
trait EventConsumer {
    def consume(event: Event): Unit
    def close(): Unit = () => ()
}


/**
  * 消费event，产出event列表
  */
class ToEventSeqConsumer() extends EventConsumer {

    private val buffer: ArrayBuffer[Event] = new ArrayBuffer[Event]()

    override def consume(event: Event): Unit = {
        buffer += event
    }

    def eventSeq: Seq[Event] = {
        buffer
    }
}




