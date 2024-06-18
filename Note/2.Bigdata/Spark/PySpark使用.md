## Pyspark 常用参数说明

```bash
/opt/cloudera/parcels/CDH/lib/spark3/bin/spark-submit \
 --queue app_enterprise_info \
 --deploy-mode client  --master yarn \
 --driver-memory 4g \
 --num-executors 4  --executor-cores 4  --executor-memory 16g \
 --conf spark.default.parallelism=50 \
  /root/yangjinhua/firstApp2.py
```

- `--queue dws_person_exhibition`: 指定在YARN中使用的队列名称，这里是`dws_person_exhibition`队列。
- `--deploy-mode client --master yarn`: 指定部署模式为客户端模式（client mode），即驱动程序在客户端机器上运行，并通过YARN管理执行器。
- `--driver-memory 4g`: 设置Spark驱动程序的内存为4GB。
- `--num-executors 4 --executor-cores 4 --executor-memory 16g`: 分别指定了总共使用4个执行器，每个执行器分配4个核心，以及每个执行器的内存大小为16GB。
- `--conf spark.driver.maxResultSize=4g`: 限制驱动程序返回结果的最大大小为4GB。
- `--conf spark.default.parallelism=50`: 设置默认的并行度为50。
- `--conf spark.kryoserializer.buffer=64m --conf spark.kryoserializer.buffer.max=2000m`: 配置Kryo序列化器的缓冲区初始大小为64MB，最大大小为2GB。
- `--conf spark.sql.shuffle.partitions=1000`: 设置SQL查询中的shuffle分区数为1000，这会影响join、groupByKey等操作的并行度。
- `--conf spark.rpc.askTimeout=1200000`: 将RPC请求的超时时间设置为20分钟（1200000毫秒），有助于避免`RejectedExecutionException`错误，如果是因为超时导致的话。
- `--conf spark.speculation=true`: 启用推测执行，可以帮助提高任务执行的总体速度，特别是在某些任务执行缓慢的情况下。
- `--conf spark.sql.files.openCostInBytes=33554432`: 设置打开文件的成本估计，影响数据本地性优化时的决策，这里设置为32MB。
- `--conf spark.sql.files.maxPartitionBytes=268435456`: 设置单个HDFS文件读取时的最大分区大小为256MB。
- `/root/osmondy/firstApp.py`: 指定要提交运行的Python应用程序路径。



## Pyspark通用执行脚本

```python
# -*- coding:utf-8 -*-
# firstApp.py
from pyspark import SparkContext
from pyspark.sql.session import SparkSession

# 以下三行为新增内容
import os, sys

# reload(sys)
# sys.setdefaultencoding('utf8')

if __name__ == "__main__":
    # sc = SparkContext(appName='dws_actor_exhibition')
    # spark = SparkSession(sc)
    spark = SparkSession.builder \
        .appName('Spark任务') \
        .config("hive.metastore.uris", "thrift://master02:9083") \
        .config("hive.merge.mapfiles", "true") \
        .config("hive.merge.mapredfiles", "true") \
        .config("hive.merge.size.per.task", "256000000") \
        .config("mapred.max.split.size", "256000000") \
        .config("mapred.min.split.size.per.node", "192000000") \
        .config("mapred.min.split.size.per.rack", "192000000") \
        .config("hive.input.format", "org.apache.hadoop.hive.ql.io.CombineHiveInputFormat") \
        .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer") \
        .config("spark.KryoSerializer.buffer.max", "2000m") \
        .config("spark.sql.hive.convertMetastoreOrc", "false") \
        .config("spark.hadoop.mapred.input.dir.recursive", "true") \
        .config("spark.sql.session.timeZone", "UTC+8") \
        .enableHiveSupport() \
        .getOrCreate()

    # 读取sql文件
    sql_url = spark.conf.get("spark.sql.url")
    with open(sql_url, 'r') as f:
        content = f.read()

    # print('原始内容::::=============================', content)
    spark.sql("set spark.sql.adaptive.enabled=true")
    spark.sql("set spark.sql.adaptive.coalescePartitions.enabled=true")
    spark.sql("set spark.sql.files.openCostInBytes=102400")
    # sql查询。
    spark.sql(content).show()
```

