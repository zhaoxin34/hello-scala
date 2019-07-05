package joky.event.creator.test

import java.util.Date

import joky.event.creator.EventCreator
import joky.event.creator.consumer.ToEventSeqConsumer
import org.scalatest.FlatSpec

class TestEventCreator extends FlatSpec {
    "EventCreator" should "create some event without error and create some events" taggedAs test.eventCreator in {
        val eventCreator = EventCreator(100, 100000)
        val eventConsumer = new ToEventSeqConsumer()
        eventCreator.consumeEvent(new Date(), 60, eventConsumer)

        println(eventConsumer.eventSeq)
        assert(eventConsumer.eventSeq.nonEmpty)
    }
}
