package joky.producer

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/29 20:04
  * @Description:
  */
object ProducerApp extends App {
    val eventProducer = EventProducer.createEventProducer()

    if (eventProducer.nonEmpty) {
        eventProducer.get.createEvent(1, 1).foreach(println)
    }
}
