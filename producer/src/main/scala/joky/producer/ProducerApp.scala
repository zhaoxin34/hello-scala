package joky.producer

import com.github.nscala_time.time.Imports._
import joky.producer.util.ConfigUtil
import org.apache.logging.log4j.scala.Logging
import org.joda.time.DateTime

/**
  * 生成器的配置
  *
  * @param visitorPoolSize                每个事件生成器，访客池的大小，建议值10-1000000
  * @param userIdPerVisitor               每个事件生成器，每个访客可以拥有的用户id数，建议1-10
  * @param eventCreateCountPerMiniute     每个事件生成器，每分钟产生的event数量
  * @param visitorUsedPerBatch            每个事件生成器，每一批次使用的访客数量，1 - visitorPoolSize
  * @param consumeDurationMiniutePerBatch 每个事件生成器，每一批次持续的时间（分钟）
  * @param outputToConsole                是否向屏幕输出，打开用于debug
  * @param outputToKafka                  是否向kafka输出
  * @param kafkaBootstrapServers          kafka server配置，如：datatist-centos00:9092,datatist-centos01:9092,datatist-centos02:9092
  * @param kafkaTopicId                   kafka的topicId
  */
case class ProducerConfig(visitorPoolSize: Int,
                          userIdPerVisitor: Int,
                          eventCreateCountPerMiniute: Int,
                          visitorUsedPerBatch: Int,
                          consumeDurationMiniutePerBatch: Int,
                          outputToConsole: Boolean = true,
                          outputToKafka: Boolean = false,
                          kafkaBootstrapServers: String,
                          kafkaTopicId: String)

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/29 20:04
  * @Description:
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

        opt[Int]("visitorPoolSize").action((x, c) =>
            c.copy(visitorPoolSize = x)).text("访客池的大小，建议值10-1000000")

        opt[Int]("userIdPerVisitor").action((x, c) =>
            c.copy(userIdPerVisitor = x)).text("每个访客可以拥有的用户id数，建议1-10")

        opt[Int]("eventCreateCountPerMiniute").action((x, c) =>
            c.copy(eventCreateCountPerMiniute = x)).text("每分钟产生的event数量")

        opt[Int]("visitorUsedPerBatch").action((x, c) =>
            c.copy(visitorUsedPerBatch = x)).text("每一批次使用的访客数量，1 - visitorPoolSize")

        opt[Int]("consumeDurationMiniutePerBatch").action((x, c) =>
            c.copy(consumeDurationMiniutePerBatch = x)).text("每一批次持续的时间（分钟）")

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
        ProducerScheduler.startSchedule(() => createEventCallback(producerConfig))
    }

    private def createEventCallback(producerConfig: ProducerConfig): Unit = {
        // 创建事件发生器
        val eventProducer = EventProducer.createEventProducer(producerConfig.visitorPoolSize, producerConfig.userIdPerVisitor, producerConfig.eventCreateCountPerMiniute)

        if (eventProducer.nonEmpty) {
            // 向屏幕输出
            if (producerConfig.outputToConsole)
                eventProducer.get.consumeEvent(DateTime.now().second(0).millis(0).getMillis, producerConfig.consumeDurationMiniutePerBatch, producerConfig.visitorUsedPerBatch, new PrintEventConsumer())

            // 向kafka输出
            if (producerConfig.outputToKafka) {

                val kafkaConfig = ConfigUtil.readPorpertiesFile("producer/src/main/resources/kafka-producer.properties")
                if (producerConfig.kafkaBootstrapServers != null)
                    kafkaConfig.put("bootstrap.servers", producerConfig.kafkaBootstrapServers)

                eventProducer.get.consumeEvent(DateTime.now().second(0).millis(0).getMillis,
                    producerConfig.consumeDurationMiniutePerBatch,
                    producerConfig.visitorUsedPerBatch,
                    new KafkaEventConsumer(producerConfig.kafkaTopicId, kafkaConfig))
            }
        }
    }


}
