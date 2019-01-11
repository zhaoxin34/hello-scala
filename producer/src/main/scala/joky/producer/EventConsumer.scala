package joky.producer

import java.util.Properties

import joky.core.bean.Event
import joky.core.util.JsonUtil
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.logging.log4j.scala.Logging

trait EventConsumer {
    def consume(event: Event): Unit
    def close(): Unit = () => ()
}

class PrintEventConsumer extends EventConsumer {
    override def consume(event: Event): Unit = println(event)
}

class KafkaEventConsumer(val topicId: String, val kafkaConfig: Properties) extends EventConsumer with Logging {

    private val producer: KafkaProducer[Nothing, String] = new KafkaProducer[Nothing, String](kafkaConfig)

    logger.info(s"Sending Records in Kafka Topic [$topicId]")

    override def consume(event: Event): Unit = {
        producer.send(new ProducerRecord(topicId, JsonUtil.toJson(event)))
    }

    override def close(): Unit = producer.close()
}
