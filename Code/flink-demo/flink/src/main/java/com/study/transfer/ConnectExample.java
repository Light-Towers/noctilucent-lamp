package com.study.transfer;

import com.study.base.Event;
import com.study.source.ClickSource2;

import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 连接流
 * 
 * @author osmondy
 */
public class ConnectExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        DataStreamSource<Integer> source1 = env.fromElements(1,2,3);
        DataStreamSource<Event> source2 = env.addSource(new ClickSource2());

        // 连接流
        ConnectedStreams<Integer, Event> connect = source1.connect(source2);

        connect.process(new CoProcessFunction<Integer,Event,String>() {

            @Override
            public void processElement1(Integer value, CoProcessFunction<Integer, Event, String>.Context ctx,
                    Collector<String> out) throws Exception {
                out.collect("source1: " + value.toString());
                
            }

            @Override
            public void processElement2(Event value, CoProcessFunction<Integer, Event, String>.Context ctx,
                    Collector<String> out) throws Exception {
                out.collect("source2: " + value.toString());
                
            }
        }).print();
        
        env.execute();
    }
}
