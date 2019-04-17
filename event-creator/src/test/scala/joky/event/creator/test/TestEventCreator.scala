package joky.event.creator.test

import joky.event.creator.EventCreator
import org.scalatest.FlatSpec

class TestEventCreator extends FlatSpec {
    "EventCreator" should "create some event without error and creat some events" taggedAs test.eventCreator in {
        val eventSeq = EventCreator.createEventList()
        assert(eventSeq.nonEmpty)
    }
}
