package joky.producer

import java.util.Date
import java.util.concurrent.TimeUnit

import joky.core.util.ConfigUtil
import joky.event.creator.EventCreator
import joky.event.creator.component.NormalActivationCuv
import joky.event.creator.consumer.EventConsumer
import org.apache.logging.log4j.scala.Logging

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._

/**
  * 启动代码示例
  * sbt ";compile;project producer;runMain joky.producer.ProducerApp --outputToKafka=true --kafkaTopicId=test-producer-20190111"
  * sbt ";compile;project producer;runMain joky.producer.ProducerApp --outputToKafka=true --outputToConsole=true --visitorPoolSize=100000 --userIdPerVisitor=3 --eventCreateCountPerSecond=10 --visitorUsedPerBatch=30 --kafkaBootstrapServers=datatist-centos00:9092,datatist-centos01:9092,datatist-centos02:9092 --kafkaTopicId=test-message"
  */

/**
  * 生成器的配置
  *
  * @param scheduleDelay    启动后延时多少
  * @param scheduleInterval 调度间隔
  *
  * @param visitorPoolSize               每个事件生成器，访客池的大小，建议值10 -- 1000000
  * @param eventCreateCountPerDay     每个事件生成器，每天产生数据量
  * @param outputToConsole               是否向屏幕输出，打开用于debug
  * @param outputToKafka                 是否向kafka输出
  * @param kafkaBootstrapServers         kafka server配置，如：datatist-centos00:9092,datatist-centos01:9092,datatist-centos02:9092
  * @param kafkaTopicId                  kafka的topicId
  */
case class ProducerConfig(scheduleDelay: Int = 0,
                          scheduleInterval: Int = 60,
                          visitorPoolSize: Int = 10,
                          eventCreateCountPerDay: Int = 10000,
                          outputToConsole: Boolean = true,
                          outputToKafka: Boolean = false,
                          kafkaBootstrapServers: String,
                          kafkaTopicId: String)

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/29 20:04
  * @Description:
  *
  * 流程：
  * 从配置文件读出配置
  * 从命令行读出配置，覆盖配置
  * 初始化事件发生器
  * 采用屏幕或者kafka输出
  */
object ProducerApp extends App with Logging {
    // read from file first
    val producerConfig: ProducerConfig = ConfigUtil.readYamlFile("producer/src/main/resources/producer.yml", classOf[ProducerConfig])

    // read from command line
    val producerConfigParser = new scopt.OptionParser[ProducerConfig]("config event producer") {
        head("=" * 10, "Event Producer App", "=" * 10)
        help("help").text("没啥写的，看代码吧")

        opt[Int]("scheduleDelay").action((x, c) =>
            c.copy(scheduleDelay = x)).text("启动后延时多少")

        opt[Int]("scheduleInterval").action((x, c) =>
            c.copy(scheduleInterval = x)).text("调度间隔")

        opt[Int]("visitorPoolSize").action((x, c) =>
            c.copy(visitorPoolSize = x)).text("访客池的大小，建议值10-1000000")

        opt[Int]("eventCreateCountPerDay").action((x, c) =>
            c.copy(eventCreateCountPerDay = x)).text("每天钟产生的event数量")

        opt[Boolean]("outputToConsole").action((x, c) =>
            c.copy(outputToConsole = x)).text("是否向屏幕输出，打开用于debug")

        opt[Boolean]("outputToKafka").action((x, c) =>
            c.copy(outputToKafka = x)).text("是否向kafka输出")

        opt[String]("kafkaBootstrapServers").action((x, c) =>
            c.copy(kafkaBootstrapServers = x)).text("kafka server配置，如：datatist-centos00:9092,datatist-centos01:9092,datatist-centos02:9092")

        opt[String]("kafkaTopicId").action((x, c) =>
            c.copy(kafkaTopicId = x)).text("kafka的topicId")
    }

    producerConfigParser.parse(args, producerConfig) match {
        case Some(config) => execute(config)
        case None => println("config error")
    }

    def execute(producerConfig: ProducerConfig): Unit = {
        logger.info(producerConfig)
        // 创建事件发生器
        val eventProducer = EventCreator(producerConfig.visitorPoolSize, producerConfig.eventCreateCountPerDay, new NormalActivationCuv(Set(8, 15)))
        ProducerScheduler.startSchedule(producerConfig.scheduleDelay, producerConfig.scheduleInterval, () => createEventCallback(eventProducer, producerConfig))
    }

    private def createEventCallback(eventCreator: EventCreator, producerConfig: ProducerConfig): Unit = {

        val consumers = new ArrayBuffer[EventConsumer]()

        // 向屏幕输出
        if (producerConfig.outputToConsole)
            consumers += new PrintEventConsumer()

        // 向kafka输出
        if (producerConfig.outputToKafka) {
            val kafkaConfig = ConfigUtil.readPorpertiesFile("producer/src/main/resources/kafka-producer.properties")
            if (producerConfig.kafkaBootstrapServers != null)
                kafkaConfig.put("bootstrap.servers", producerConfig.kafkaBootstrapServers)

            consumers += new KafkaEventConsumer(producerConfig.kafkaTopicId, kafkaConfig)
        }

        eventCreator.consumeEvent(new Date(), producerConfig.scheduleInterval, consumers: _*)
    }


}
