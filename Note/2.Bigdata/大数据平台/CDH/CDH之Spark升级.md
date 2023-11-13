## 一、背景

CDH中Spark默认版本2.4.0，我们对Hive升级到3.1.3版本，由于并未找到对应的 spark-hive 包，于是尝试使用Spark-3.3.1。

[spark3.3.1 for CDH6.3.2 包下载链接](https://download.csdn.net/download/qq_36610426/87125679)

## 二、安装 Spark3-cdh

### 2.1 备份

```bash
cd /opt/cloudera/parcels/CDH/lib
cp -r spark/ spark240.hive211.bak
```

### 2.2 安装

```bash
# 解压
cd /opt/software/spark/
tar -zxvf spark-3.3.1-bin-3.0.0-cdh6.3.2.tgz
# 拷贝
cp -r spark-3.3.1-bin-3.0.0-cdh6.3.2 /opt/cloudera/parcels/CDH/lib/spark3
```

### 2.3 配置文件

```bash
# 拷贝 hive-site.xml
cp /etc/hive/conf/hive-site.xml /opt/cloudera/parcels/CDH/lib/spark3/conf/
# 拷贝spark配置文件
# 拷贝 spark-env.sh
cp /etc/spark/conf/spark-env.sh  /opt/cloudera/parcels/CDH/lib/spark3/conf/
# 拷贝 classpath.txt
cp /etc/spark/conf/classpath.txt  /opt/cloudera/parcels/CDH/lib/spark3/conf/
# 拷贝 spark-defaults.conf
cp /etc/spark/conf/spark-defaults.conf /opt/cloudera/parcels/CDH/lib/spark3/conf/
# 拷贝 yarn-site.xml
cp -r /etc/spark/conf/yarn-conf/yarn-site.xml /opt/cloudera/parcels/CDH/lib/spark3/conf/
```

### 2.4 修改配置

* `vim spark-env.sh`

  ~~~shell
  ...
  SELF="$(cd $(dirname $BASH_SOURCE) && pwd)"
  if [ -z "$SPARK_CONF_DIR" ]; then
    export SPARK_CONF_DIR="$SELF"
  fi
  
  # 更改一下 SPARK_HOME
  #export SPARK_HOME=/opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/spark
  export SPARK_HOME=/opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/spark3
  
  SPARK_PYTHON_PATH=""
  if [ -n "$SPARK_PYTHON_PATH" ]; then
    export PYTHONPATH="$PYTHONPATH:$SPARK_PYTHON_PATH"
  fi
  ...
  ~~~

* `vim spark-defaults.conf`

  1. 修改`spark.yarn.jars`路径

  2. 注释`lineage`相关的(暂时)
  3. 兼容老的通讯协议

  ```bash
  spark.authenticate=false
  spark.driver.log.dfsDir=/user/spark/driverLogs
  spark.driver.log.persistToDfs.enabled=true
  spark.dynamicAllocation.enabled=true
  spark.dynamicAllocation.executorIdleTimeout=60
  spark.dynamicAllocation.minExecutors=0
  spark.dynamicAllocation.schedulerBacklogTimeout=1
  spark.eventLog.enabled=true
  spark.io.encryption.enabled=false
  spark.network.crypto.enabled=false
  spark.serializer=org.apache.spark.serializer.KryoSerializer
  spark.shuffle.service.enabled=true
  spark.shuffle.service.port=7337
  spark.ui.enabled=true
  spark.ui.killEnabled=true
  # spark.lineage.log.dir=/var/log/spark/lineage
  # spark.lineage.enabled=true
  spark.master=yarn
  spark.submit.deployMode=client
  spark.eventLog.dir=hdfs://master01:8020/user/spark/applicationHistory
  spark.yarn.historyServer.address=http://master02:18088
  spark.yarn.jars=local:/opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/spark3/jars/*	 # 修改`spark.yarn.jars`路径
  spark.driver.extraLibraryPath=/opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/hadoop/lib/native
  spark.executor.extraLibraryPath=/opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/hadoop/lib/native
  spark.yarn.am.extraLibraryPath=/opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/hadoop/lib/native
  spark.yarn.config.gatewayPath=/opt/cloudera/parcels
  spark.yarn.config.replacementPath={{HADOOP_COMMON_HOME}}/../../..
  spark.yarn.historyServer.allowTracking=true
  spark.yarn.appMasterEnv.MKL_NUM_THREADS=1
  spark.executorEnv.MKL_NUM_THREADS=1
  spark.yarn.appMasterEnv.OPENBLAS_NUM_THREADS=1
  spark.executorEnv.OPENBLAS_NUM_THREADS=1
  # spark.extraListeners=com.cloudera.spark.lineage.NavigatorAppListener
  # spark.sql.queryExecutionListeners=com.cloudera.spark.lineage.NavigatorQueryListener
  spark.shuffle.useOldFetchProtocol=true 	#兼容老的通讯协议
  ```

## 三、编辑一个spark-sql

* `vim /opt/cloudera/parcels/CDH/bin/spark-sql`

  ```````shell
  ...
  #!/bin/bash  
  # Reference: http://stackoverflow.com/questions/59895/can-a-bash-script-tell-what-directory-its-stored-in  
  export HADOOP_CONF_DIR=/etc/hadoop/conf
  export YARN_CONF_DIR=/etc/hadoop/conf
  SOURCE="${BASH_SOURCE[0]}"  
  BIN_DIR="$( dirname "$SOURCE" )"  
  while [ -h "$SOURCE" ]  
  do  
   SOURCE="$(readlink "$SOURCE")"  
   [[ $SOURCE != /* ]] && SOURCE="$BIN_DIR/$SOURCE"  
   BIN_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"  
  done  
  BIN_DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"  
  LIB_DIR=$BIN_DIR/../lib  
  export HADOOP_HOME=$LIB_DIR/hadoop  
  
  # Autodetect JAVA_HOME if not defined  
  . $LIB_DIR/bigtop-utils/bigtop-detect-javahome  
  
  exec $LIB_DIR/spark3/bin/spark-submit --class org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver "$@"
  ...
  ```````

完成后使用 alternatives 进行环境变量管控

```bash
alternatives --install /usr/bin/spark-sql spark-sql /opt/cloudera/parcels/CDH/bin/spark-sql 1
alternatives --config spark-sql
```

如果有多个版本，切换为刚刚配置的

## 参考

1. [spark3.3.1 for CDH6.3.2 打包](https://blog.csdn.net/qq_36610426/article/details/127997188?utm_medium=distribute.pc_relevant.none-task-blog-2~default~baidujs_baidulandingword~default-0-127997188-blog-126360600.235^v38^pc_relevant_anti_t3_base&spm=1001.2101.3001.4242.1&utm_relevant_index=3)

2. [CDH6.3.2 升级 Spark3.3.0 版本](https://juejin.cn/post/7140053569431928845)
3. [Spark错误之 Unknown message type: 10](https://blog.csdn.net/weixin_48231806/article/details/125097927)
