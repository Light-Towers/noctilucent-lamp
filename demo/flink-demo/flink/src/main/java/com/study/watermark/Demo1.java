package com.study.watermark;

import java.time.Duration;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import com.study.base.Event;
import com.study.source.ClickSource;

/**
 * @author osmondy
 */
public class Demo1 {
    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Event> stream = env.addSource(new ClickSource());

        
        // 水位线策略
        // 有序流(时间戳单调递增)
        WatermarkStrategy<Event> forMonotonousTimestamps = WatermarkStrategy.<Event>forMonotonousTimestamps().withTimestampAssigner(
            new SerializableTimestampAssigner<Event>(){
                // 抽取时间戳逻辑
                @Override
                public long extractTimestamp(Event element, long recordTimestamp) {
                    // 告诉 Flink 数据源里的时间戳是哪一个字段， 如果不指定，则会指定为 recordTimestamp = -9223372036854775808
                    return element.timestamp;
                }
        });

        
        // 处理乱序流
        WatermarkStrategy<Event> forBoundedOutOfOrderness = WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(5)).withTimestampAssigner(
            new SerializableTimestampAssigner<Event>(){
                // 抽取时间戳逻辑
                @Override
                public long extractTimestamp(Event element, long recordTimestamp) {
                    // 告诉 Flink 数据源里的时间戳是哪一个字段
                    return element.timestamp;
                }
        });


        // 指配水位线策略
        DataStream<Event> watermarkStream = stream.assignTimestampsAndWatermarks(forMonotonousTimestamps).assignTimestampsAndWatermarks(forBoundedOutOfOrderness);

        watermarkStream.print();
        env.execute();
    }

    

}
