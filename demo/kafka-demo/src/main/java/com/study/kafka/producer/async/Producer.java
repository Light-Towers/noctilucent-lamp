package com.study.kafka.producer.async;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Producer {
    public static void main(String[] args) {
        // 1. 配置生产者客户端参数及创建响应生产者实例
        Properties properties = new Properties();
        // 连接集群 bootstrap.servers
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"centos31:9092,centos32:9092,centos33:9092");
        // 指定对应的key和value的序列化类型 key.serializer
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        // 验证幂等性
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        // 创建kafka生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);
        // 2. 构建待发送的消息

        //向分区1发数据
        ProducerRecord<String, String> record = new ProducerRecord<>("second-test", "test ----- Hello, kafka");


        // 3. 发送数据
        try {
            for (int i = 0; i < 10000; i++) {
//                record = new ProducerRecord<>("second", "Hello, kafka6");
                producer.send(record);
            }
            // a. 发后即忘。 send() 默认异步
//            producer.send(record);

//            // b. 异步回调
//            producer.send(record, new Callback() {
//                // RecordMetadata 与 Exception 存在互斥
//                @Override
//                public void onCompletion(RecordMetadata metadata, Exception e) {
//                    if (e == null){
//                        System.out.println("topic："+metadata.topic() + " partition："+ metadata.partition());
//                    } else {
//                        // 记录错误日志 或 处理让消息重发
//                        e.printStackTrace();
//                    }
//                }
//            });
//            // c. 同步
//            producer.send(record).get();
        } catch (Exception e) {
            // 记录错误日志 或 处理让消息重发
            e.printStackTrace();
        }
        // 4. 关闭资源
        producer.close();
    }
}
