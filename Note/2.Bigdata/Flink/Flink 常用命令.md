# 任务提交

## per-job模式

```bash
flink run -t yarn-per-job -c com.SourceHive xx.jar

# 提交到yarn集群
flink run -m yarn-cluster \
 -p 10 -yjm 2048 -ytm 2048 -d \
 -Dlog.file=/tmp/flink-job.log \
 -Dlog.level=INFO \
 -Dyarn.application.name=test-kafka2doris \
 -c com.KafkaSourceToDorisJdbc \
 /root/flink/warehouse-flink-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## 应用模式提交application，适用于生产环境
```bash
flink run-application -t yarn-application \
 -Dyarn.application.name="test-kafka2doris" \
 -Dparallelism.default=30  \
 -Djobmanager.memory.process.size=2048m \
 -Dtaskmanager.memory.process.size=4096m \
 -Dtaskmanager.numberOfTaskSlots=10 \
 -c com.KafkaSourceToDorisJdbc \
 /root/flink/warehouse-flink-1.0-SNAPSHOT.jar
```

# Flink 一些常用命令

```bash
## flink 手动触发保存点
bin/flink savepoint :jobId [:savepoint_directory]
# 取消任务并触发保存点
bin/flink cancel -s <savepoint_directory> <job_id>

## flink cdc 从某个保存点恢复
bin/flink-cdc.sh -s hdfs://master01:8020/flink/state/flink-savepoints/savepoint-f61e01-69c7cb26ba26 mysql-to-doris.yaml
```

