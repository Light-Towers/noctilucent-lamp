# 一、测试EMR

hadoop webUI：<http://172.16.9.91:9870>

hdfs roc: hdfs://172.16.9.91:9000

hdfs webUI: <http://172.16.9.91:9870/explorer.html#/>

yarn webUI：<http://172.16.9.91:8088/cluster>

## Hive

### 链接方式

```bash
beeline -u jdbc:hive2://master-1-1.c-dc3df4e152d1589d.cn-beijing.emr.aliyuncs.com:10000
```



# Spark

【spark示例文档】：https://sparkbyexamples.com/spark/spark-write-dataframe-to-csv-file/

Spark context Web UI available at http://172.16.9.91:4040

spark history：http://172.16.9.91:18080/

Livy: http://172.16.9.91:8998/





测试环境

- mysql：
  - 172.16.9.89:3306
  - usr: root
  - pwd: 123456
- mongo:
  - 172.16.9.89:27017
  - usr: admin
  - pwd: 123456
- redis:
  - 172.16.9.89:6371,172.16.9.89:6372,172.16.9.89:6373,172.16.9.90:6374,172.16.9.90:6375,172.16.9.90:6376
- kafka:
  - 172.16.9.89:9092,172.16.9.89:9093,172.16.9.89:9094
- kafka-manager
  - 172.16.9.89:9000
- nacos
  - 172.16.9.89:8848,172.16.9.89:8851,172.16.9.89:8853
- sentinel
  - 172.16.9.89:8888
- zipkin
  - 172.16.9.89:9411
- xxl-job
  - 172.16.9.89:8080
- elasticsearch
  - 172.16.9.89:9200,172.16.9.89:9201,172.16.9.89:9202
- kibana
  - 172.16.9.89:5601
- prometheus
  - 172.16.9.89:9090
- pushgateway
  - 172.16.9.89:9091
- grafana
  - 172.16.9.89:3000
- zookeeper
  - 172.16.9.89:2181





# 二、生产配置

* livy：http://10.130.8.213:8998

* Oss: 

* Spark: https://10.130.8.212:8443/gateway/yarnui/sparkhistory/
  * 账号：oss
    密码：EmrOss123
* yarn UI: https://10.130.8.212:8443/gateway/yarnui/yarn/cluster/scheduler



emr
  ip:10.130.8.212
  pw:WPDdL@uz2
emr_gateway:
  ip:10.130.8.217
  pw:WPDdL@uz2Gateway
emr_jupyter:

  ip:10.130.8.224
  WPDdL@uz2Jupyter
emr_presto:

  ip:10.130.8.243
  WPDdL@uz2Presto

  user:oss
  pwd:oss_presto_ui_user_pwd

HBase

  ip: 10.130.9.2,10.130.9.3,10.130.9.4
  root:WPDdL@uz2

