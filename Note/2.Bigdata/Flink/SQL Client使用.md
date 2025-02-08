[Flink SQL 客户端如何使用](https://cloud.tencent.com/developer/article/1840215)

```sql
CREATE CATALOG hive_catalog WITH (
    'type' = 'hive',
    'default-database' = 'flink_db',
    'hive-conf-dir' = '/usr/hdp/3.1.5.0-152/hive/conf',
    'hadoop-conf-dir' = '/etc/hadoop/conf'
);
-- set the HiveCatalog as the current catalog of the session
USE CATALOG hive_catalog;
```

```sql
-- 设置Hive方言,否则使用Flink默认方言
SET table.sql-dialect = hive;
-- 设置视图
SET sql-client.execution.result-mode = tableau;
-- 时区设置
SET 'table.local-time-zone' = 'Asia/Shanghai';   -- 'UTC'

-- 加载Hive模块，可使用Hive函数
Load module hive with ('hive-version'='3.1.3');
```

## 使用SQL文件初始化会话

* 定义初始化SQL文件

```sql
-- 定义 Catalogs
CREATE CATALOG MyCatalog
  WITH (
    'type' = 'hive'
  );

USE CATALOG MyCatalog;

-- 定义 DataBase
CREATE DATABASE MyDatabase;

USE MyDatabase;

-- 定义 TABLE
CREATE TABLE MyTable (
  MyField1 INT,
  MyField2 STRING
) WITH (
  'connector' = 'filesystem',
  'path' = '/path/to/something',
  'format' = 'csv'
);

-- 定义 VIEW
CREATE VIEW MyCustomView AS SELECT MyField2 FROM MyTable;

-- 定义用户自定义函数
CREATE FUNCTION foo.bar.AggregateUDF AS myUDF;

-- 修改属性
SET table.planner = blink; -- planner: either blink (default) or old
SET execution.runtime-mode = streaming; -- execution mode either batch or streaming
SET sql-client.execution.result-mode = table; -- available values: table, changelog and tableau
SET sql-client.execution.max-table-result.rows = 10000; -- optional: maximum number of maintained rows
SET parallelism.default = 1; -- optional: Flinks parallelism (1 by default)
SET pipeline.auto-watermark-interval = 200; -- optional: interval for periodic watermarks
SET pipeline.max-parallelism = 10; -- optional: Flink's maximum parallelism
SET table.exec.state.ttl = 1000; -- optional: table program's idle state time
SET restart-strategy = fixed-delay;

SET table.optimizer.join-reorder-enabled = true;
SET table.exec.spill-compression.enabled = true;
SET table.exec.spill-compression.block-size = 128kb;

-- 设置全局并行度
SET 'parallelism.default' = '20';
-- 设置 Hive 源的并行度自动推导关闭，推断最大并行度设置为 50
SET 'table.exec.hive.infer-source-parallelism' = 'false';
SET 'table.exec.hive.infer-source-parallelism.max' = '50';
SET 'write.batch.size' = '10000';   -- 批量写入大小
-- 启用 mini-batch 处理
SET 'table.exec.mini-batch.enabled' = 'true';
SET 'table.exec.mini-batch.allow-latency' = '5s';
SET 'table.exec.mini-batch.size' = '5000';

-- 设置 Checkpoint 配置
SET 'execution.checkpointing.interval' = '1min';
SET 'execution.checkpointing.async' = 'true';
SET 'execution.checkpointing.timeout' = '10min';
```

* 执行

```bash
sql-client.sh -i /data/flink/init.sql
```





> 难：
>
> https://juejin.cn/post/7003903405005471757
