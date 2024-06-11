spark 常用参数说明

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
- `/root/osmondy/firstApp2.py`: 指定要提交运行的Python应用程序路径。
