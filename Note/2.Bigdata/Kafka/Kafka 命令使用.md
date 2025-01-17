> 节点信息：centos31:9092,centos32:9092,centos33:9092
# topic命令
```bash
# 查看topic列表
bin/kafka-topics.sh --bootstrap-server centos31:9092 --list
# 创建topic
bin/kafka-topics.sh --bootstrap-server centos31:9092 --create --partitions 3 --replication-factor 2 --topic first
# 查看topic详情
bin/kafka-topics.sh --bootstrap-server centos31:9092 --describe --topic first
# 修改分区数（注意：分区数只能增加，不能减少）
bin/kafka-topics.sh --bootstrap-server centos31:9092 --alter --topic first --partitions 3
# 删除 topic
bin/kafka-topics.sh --bootstrap-server centos31:9092 --delete --topic first
```

```bash
# 查看某个topic的offset
bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list centos31:9092 --topic first
```

# 生产者
```bash
# 生产者发送消息
bin/kafka-console-producer.sh --bootstrap-server centos31:9092 --topic first
```

# 消费者/组
```bash
# 消费某主题中的数据
# 消费历史数据（从头开始） --from-beginning
bin/kafka-console-consumer.sh --bootstrap-server centos31:9092 --topic first



# 查看所有消费者组
bin/kafka-consumer-groups.sh --bootstrap-server centos31:9092 --list
# 查看具体消费者组详情
bin/kafka-consumer-groups.sh --bootstrap-server centos31:9092 --group group_id5 --describe
```