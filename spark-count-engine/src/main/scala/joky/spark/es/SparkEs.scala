package joky.spark.es

import com.google.common.base.Stopwatch
import joky.spark.count.engine.util.SparkBuilder

/**
  * @Auther: zhaoxin
  * @Date: 2019/9/5 15:16
  * @Description:
  */
object SparkEs extends App {
    val sparkSession = SparkBuilder.build("spark es")
    sparkSession.read.format("org.elasticsearch.spark.sql").option("nodes", "192.168.0.204").option("net.http.auth.user", "elastic").option("net.http.auth.pass", "changeme")
        .load("event_*/doc")
        .createOrReplaceTempView("event")

    val stopwatch = new Stopwatch()
    stopwatch.start()
    sparkSession.read.format("org.elasticsearch.spark.sql").option("nodes", "192.168.0.204").option("net.http.auth.user", "elastic").option("net.http.auth.pass", "changeme")
        .load("cust_info_collect_es/doc")
        .createOrReplaceTempView("users")
    println(stopwatch.stop())
//    val ctx = sparkSession.sqlContext
//
//    ctx.sql(   "CREATE TEMPORARY TABLE event " +
//        "USING org.elasticsearch.spark.sql " +
//        "OPTIONS (resource 'event_*/doc', nodes '192.168.0.204', net.http.auth.user 'elastic', net.http.auth.pass 'changeme')" )

    stopwatch.start()
    sparkSession.sql("select * from event where eventTime > 1567668406000 and eventTime < 1567668476000").show()
    println(stopwatch.stop())

    stopwatch.start()
    sparkSession.sql("select count(*) from event where eventTime > 1567668406000 and eventTime < 1567668476000").show()
    println(stopwatch.stop())

    stopwatch.start()
    sparkSession.sql("select count(*) from users where cust_id = '10000095311'").show()
    println(stopwatch.stop())

    stopwatch.start()
    sparkSession.sql("select * from users where cust_id = '10000095311'").show()
    println(stopwatch.stop())

}
