package com.study.kafka.producer.async;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.Future;

public class ProducerCallback {

    public static void main(String[] args) throws InterruptedException {

        // 0 配置
        Properties properties = new Properties();

        // 连接集群 bootstrap.servers
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"10.130.8.206:9092,10.130.8.207:9092,10.130.8.208:9092");
//        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"centos31:9092,centos32:9092,centos33:9092");
//        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.29.2.13:9092");

        // 指定对应的key和value的序列化类型 key.serializer
//        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        // 1 创建kafka生产者对象
        // "" hello
        KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(properties);

        // 2 发送数据
        for (int i = 0; i < 500; i++) {
//            Future<RecordMetadata> future = kafkaProducer.send(new ProducerRecord<>("first", "study" + i));


            kafkaProducer.send(new ProducerRecord<>("myFirstStream", "study" + i), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {

                    if (exception == null){
                        System.out.println("topic： "+metadata.topic() + " partition： "+ metadata.partition());
                    } else {
                        exception.printStackTrace();
                    }
                }
            });

            Thread.sleep(1000);
        }

        // 3 关闭资源
        kafkaProducer.close();
    }
}
