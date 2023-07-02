[Flink SQL 客户端如何使用](https://cloud.tencent.com/developer/article/1840215)

```sql
CREATE CATALOG myhive WITH (
    'type' = 'hive',
    'default-database' = 'flink_database',
    'hive-conf-dir' = '/usr/hdp/3.1.5.0-152/hive/conf'
);
-- set the HiveCatalog as the current catalog of the session
USE CATALOG myhive;

```

```sql
-- 设置方言
SET table.sql-dialect=hive;
-- 设置视图
SET sql-client.execution.result-mode = tableau;
-- 时区设置
SET 'table.local-time-zone' = 'Asia/Shanghai';
SET 'table.local-time-zone' = 'UTC';
```

