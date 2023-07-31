### 背景

​	DolphinScheduler 集群环境下，zk需要 3.4.6+

### 准备

- 下载Zookeeper-3.5.5并解压：

  ```bash
  cd /opt/software/zookeeper
  wget https://archive.apache.org/dist/zookeeper/zookeeper-3.5.5/apache-zookeeper-3.5.5-bin.tar.gz
  tar -xzvf apache-zookeeper-3.5.5-bin.tar.gz
  ```

- 下载Cloudeara版本的zookeeper jar包

  ```bash
  wget https://repository.cloudera.com/artifactory/cloudera-repos/org/apache/zookeeper/zookeeper/3.5.5.7.2.16.0-287/zookeeper-3.5.5.7.2.16.0-287.jar
  ```

  

![](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image2023-7-31_10-32-49.png)

- 备份CDH的zk相关包

  ```bash
  # 备份 zookeeper jar包
  cp  /opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/jars/zookeeper-3.4.5-cdh6.3.2.jar  /opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/jars/zookeeper-3.4.5-cdh6.3.2.jar.bak
  # 备份 zookeeper 依赖的 lib 目录
  cp -ra  /opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/zookeeper/lib /opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/zookeeper/lib.bak
  ```

### 开始

- 将下载的 zookeeper-3.5.5.7.2.16.0-287.jar 拷贝到CDH中，并替换zookeeper-3.4.5-cdh6.3.2.jar

  ```bash
  cp /opt/software/zookeeper/zookeeper-3.5.5.7.1.6.16-2.jar /opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/jars/zookeeper-3.4.5-cdh6.3.2.jar
  ```

- 将下载的 zookeeper-3.5.5 的lib目录下的jar包拷贝过来，重复的jar包可以覆盖掉

  ```bash
  cp /opt/software/zookeeper/apache-zookeeper-3.5.5-bin/lib/*  /opt/cloudera/parcels/CDH-6.3.2-1.cdh6.3.2.p0.1605554/lib/zookeeper/lib/
  ```

- 重启 ZK 集群

![](https://raw.githubusercontent.com/Light-Towers/picture/master/noctilucent-lamp/image2023-7-31_10-32-2.png)