package joky.test

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.Base64
import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

/**
  * @Auther: zhaoxin
  * @Date: 2018/12/14 11:16
  * @Description:
  */
object HbaseTest extends App {

    def convertScanToString(scan: Scan): String = {
        val proto = ProtobufUtil.toScan(scan)
        Base64.encodeBytes(proto.toByteArray)
    }

    val spark = SparkSession.builder().appName("test").master("local[2]")
    val hbaseConf = HBaseConfiguration.create()

    hbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
    hbaseConf.set("hbase.zookeeper.quorum", "datatist-centos01,datatist-centos02,datatist-centos00")
    hbaseConf.set("hbase.master", "datatist-centos03:60000")
    hbaseConf.set(TableInputFormat.INPUT_TABLE, "test:test_table")

    val scan = new Scan()
    scan.setStartRow("0".getBytes())
    scan.setStopRow("2".getBytes())
    scan.setCaching(500)
    // nouse
    //    scan.setMaxResultSize(3)
    //    scan.setMaxResultsPerColumnFamily(2)
    scan.setCacheBlocks(false)
    hbaseConf.set(TableInputFormat.SCAN, convertScanToString(scan))

    val sparkConf = new SparkConf()
    sparkConf.setAppName("test hbase").setMaster("local[2]")
    val sparkContext = SparkContext.getOrCreate(sparkConf)
    val rdd = sparkContext.newAPIHadoopRDD(hbaseConf, classOf[TableInputFormat], classOf[ImmutableBytesWritable], classOf[Result])
    val count = rdd.count()
    println("-----------------" + count)

    rdd.foreach(r => {
        println("rowKey:" + new String(r._1.get()))
        r._2.rawCells().toList.foreach(cell => {
            println(new String(cell.getFamilyArray) + ":" + new String(cell.getQualifierArray) + "=" + new String(cell.getValueArray) )
        })
    })

}
