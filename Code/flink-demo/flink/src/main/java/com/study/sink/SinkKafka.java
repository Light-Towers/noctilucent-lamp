package com.study.sink;

import com.study.base.Event;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


/**
 * @author osmondy
 */
public class SinkKafka {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 模拟一个 StreamSource
        DataStreamSource<Event> stream = env.fromElements(
                new Event("Mary", "./home", 1000L),
                new Event("Bob", "./cart", 2000L),
                new Event("Alice", "./prod?id=100", 3000L),
                new Event("Alice", "./prod?id=200", 3500L),
                new Event("Bob", "./prod?id=2", 2500L),
                new Event("Alice", "./prod?id=300", 3600L),
                new Event("Bob", "./home", 3000L),
                new Event("Bob", "./prod?id=1", 2300L),
                new Event("Bob", "./prod?id=3", 3300L));


        KafkaSink<String> sink = KafkaSink.<String>builder()
        .setBootstrapServers("172.16.9.89:9092,172.16.9.89:9093,172.16.9.89:9094")
        .setRecordSerializer(KafkaRecordSerializationSchema.builder()
            .setTopic("clicks")
            .setValueSerializationSchema(new SimpleStringSchema())
            .build()
        )
        .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
        .build();

        stream.map(Event::toString).sinkTo(sink);
        env.execute();
    }
}