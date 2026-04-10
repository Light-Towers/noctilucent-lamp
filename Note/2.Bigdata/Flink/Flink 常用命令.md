# Flink 常用运维命令

本笔记汇总了 Flink 在 YARN 集群模式下的提交方式及日常运维常用指令。

---

## 1. 任务提交 (Job Submission)

### 1.1 Per-Job 模式 (YARN)
适用于资源隔离要求较高的场景。
```bash
# 基础提交
flink run -t yarn-per-job -c com.example.MyJob xx.jar

# 完整配置提交
flink run -m yarn-cluster \
 -p 10 -yjm 2048 -ytm 2048 -d \
 -Dlog.file=/tmp/flink-job.log \
 -Dlog.level=INFO \
 -Dyarn.application.name=test-job \
 -c com.study.source.SourceKafka \
 /path/to/your-jar-with-dependencies.jar
```

### 1.2 Application 模式 (YARN) - 生产环境推荐
Main 方法在 JobManager 运行，减轻客户端压力。
```bash
flink run-application -t yarn-application \
 -Dyarn.application.name="prod-job" \
 -Dparallelism.default=30  \
 -Djobmanager.memory.process.size=2048m \
 -Dtaskmanager.memory.process.size=4096m \
 -Dtaskmanager.numberOfTaskSlots=10 \
 -c com.study.sink.SinkMysql \
 /path/to/your-jar.jar
```

---

## 2. 状态管理 (Savepoint & Checkpoint)

### 2.1 手动触发保存点 (Savepoint)
用于升级程序或迁移集群。
```bash
# 手动触发
bin/flink savepoint <jobId> [hdfs://path/to/savepoint]

# 取消任务并触发保存点 (优雅停机)
bin/flink cancel -s <savepoint_path> <job_id>
```

### 2.2 从状态恢复
```bash
# 普通任务从 Savepoint 启动
flink run -s <savepoint_path> -c com.example.MyJob xx.jar

# Flink CDC 从保存点恢复
bin/flink-cdc.sh -s hdfs://master01:8020/flink/state/savepoints/savepoint-xx mysql-to-doris.yaml
```

---

## 3. 查看与管理
```bash
# 列出所有运行中的任务
flink list

# 查看任务详情
flink list -r

# 停止任务 (不带保存点)
flink stop <job_id>
```


