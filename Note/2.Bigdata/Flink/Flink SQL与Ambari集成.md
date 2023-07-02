# Flink SQL集成Ambari,实现Mongo到Hive的数据同步

## 版本信息

​	Apache Ambari:2.7.5.0 + HDP: 3.1.5.0-152

​	Hadoop: 3.1.1

​	Hive: 3.1.0

​	Flink: 1.16.2

> 参考Flink官网配置：https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/table/hive/overview/

## Hadoop环境配置

由于Flink与Ambari并未安装在同一台机器上，而Flink的Hive SQL需要Hadoop的环境，于是从Ambari机器上拷贝Hadoop+Hive的相关包。包信息如下：

```bash
drwxr-xr-x  4 root root   34 Jun  1 07:55 atlas
drwxr-xr-x  9 root root 4096 Jun  1 07:57 hadoop
drwxr-xr-x  7 root root 4096 Jun  1 07:55 hadoop-hdfs
drwxr-xr-x  6 root root 4096 Jun  1 07:57 hadoop-mapreduce
drwxr-xr-x  9 root root 4096 Jun  1 07:56 hadoop-yarn
drwxr-xr-x 11 root root  165 Jun 21 06:56 hive
drwxrwxr-x  7 1000  992   68 Jun  1 08:02 hive-hcatalog
```

配置环境变量：

```bash
# jdk
export JAVA_HOME=/usr/java/jdk1.8.0_311
export CLASSPATH=$:CLASSPATH:$JAVA_HOME/lib/
export PATH=$PATH:$JAVA_HOME/bin
# hadoop
export HADOOP_HOME=/usr/hdp/3.1.5.0-152/hadoop
export HADOOP_MAPRED_HOME=/usr/hdp/3.1.5.0-152/hadoop-mapreduce
export HADOOP_YARN_HOME=/usr/hdp/3.1.5.0-152/hadoop-yarn
export HADOOP_LIBEXEC_DIR=$HADOOP_HOME/libexec
export PATH=$PATH:$HADOOP_HOME/bin
# hive
export HIVE_HOME=/usr/hdp/3.1.5.0-152/hive
export PATH=$PATH:$HIVE_HOME/bin
```

加载变量配置 `source /etc/profile`	

## Flink 配置

1. 将 flink-sql-connector-hive-3.1.2_2.12-1.16.2.jar[(下载)](https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-hive-3.1.2_2.12/1.16.2/flink-sql-connector-hive-3.1.2_2.12-1.16.2.jar)和 flink-sql-connector-mongodb-1.0.1-1.16.jar[(下载)](https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-mongodb/1.0.1-1.16/flink-sql-connector-mongodb-1.0.1-1.16.jar)包拷贝到lib目录下。

   有2种添加Hive依赖的方式，因为有对应hive版本的flink捆绑包，此处选择第一种方式。

   > [官方原文](https://nightlies.apache.org/flink/flink-docs-release-1.16/docs/connectors/table/hive/overview/#dependencies)：
   >
   > There are two ways to add Hive dependencies. First is to use Flink’s bundled Hive jars. You can choose a bundled Hive jar according to the version of the metastore you use. Second is to add each of the required jars separately. The second way can be useful if the Hive version you’re using is not listed here.
   >
   > **NOTE**: the recommended way to add dependency is to use a bundled jar. Separate jars should be used only if bundled jars don’t meet your needs.

2. 移动 `planner` 相关jar包

   ```bash
   mv $FLINK_HOME/opt/flink-table-planner_2.12-1.16.2.jar $FLINK_HOME/lib/flink-table-planner_2.12-1.16.2.jar
   mv $FLINK_HOME/lib/flink-table-planner-loader-1.16.2.jar $FLINK_HOME/opt/flink-table-planner-loader-1.16.2.jar
   ```

3. 配置Hadoop依赖环境

   ```bash
   export HADOOP_CLASSPATH=`hadoop classpath`
   ```

4. 启动Flink

   ```bash
   bin/start-cluster.sh
   ```

5. 开启Flink SQL客户端

   ```bash
   bin/sql-client.sh
   ```

## Mongo 数据同步到 Hive


1. 创建 Mongo 关联表

   Mongo对应的表中存在1000条测试数据

   ```bash
   # 设置方言
   set table.sql-dialect=default;
   # 创建Mongo关联表
   CREATE TABLE mongo_test (
     _id STRING,
     PID STRING,
     NAME STRING,
     JSON_ARR ARRAY<STRING>,
     PRIMARY KEY (_id) NOT ENFORCED
   ) WITH (
      'connector' = 'mongodb',
      'uri' = 'mongodb://admin:123456@bigdata:27017',
      'database' = 'Test',
      'collection' = 'mongo_test'
   );
   ```
   
2. 创建 Hive 表

   ```bash
   CREATE CATALOG myhive WITH (
       'type' = 'hive',
       'default-database' = 'flink_database',	#需要在hive中创建对应的库
       'hive-conf-dir' = '/usr/hdp/3.1.5.0-152/hive/conf'	#对应的hive配置文件
   );
   # 使用 CATALOG
   USE CATALOG myhive;
   
   # 设置方言
   set table.sql-dialect=hive;
   # 创建 Hive 表
   CREATE TABLE hive_test (
     _id STRING,
     PID STRING,
     NAME STRING,
     JSON_ARR ARRAY<STRING>
   ) 
   LOCATION
     'hdfs://bigdata:8020/warehouse/external/flink_database.db/hive_test'; 
   ```

3. Mongo数据同步Hive

   ```bash
   INSERT INTO hive_test SELECT * FROM mongo_test;
   ```


