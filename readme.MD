# 查看依赖的命令
dependencyTree
dependencyBrowseGraph
whatDependsOn <organization> <module> <revision>?: Find out what depends on an artifact. Shows a reverse dependency tree for the selected module. The <revision> argument is optional.

# 执行某个单个测试用例
如：testOnly joky.event.creator.test.TestActivationCuv


# 执行spark
[zhaoxin@zhaoxindeMacBook-Pro(office) ~/working/life/bigdata/spark-2.4.4-bin-hadoop2.7]$
0> ./bin/spark-submit --master spark://spark-master-0.spark-master-svc.dev-bigdata.svc.cluster.local:7077 --conf spark.driver.host=192.168.0.124 --conf spark.driver.bindAddress=192.168.0.124 --conf spark.sql.shuffle.partitions=2 --name=test --class joky.spark.count.engine.Starter3 --conf spark.cores.max=2 --executor-memory=512m /Users/zhaoxin/working/life/scala/hello-scala/spark-count-engine/target/spark-count-engine-assembly-0.1.0-SNAPSHOT.jar