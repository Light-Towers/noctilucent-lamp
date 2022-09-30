package com.study.transfer;

import com.study.base.Event;
import com.study.base.util.TimeUtil;
import com.study.source.ClickSource;
import com.study.source.ClickSource2;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 合流
 * 
 * @author osmondy
 */
public class UnionExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> source1 = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                            @Override
                            public long extractTimestamp(Event event, long l) {
                                return event.timestamp;
                            }
                        }));
        SingleOutputStreamOperator<Event> source2 = env.addSource(new ClickSource2())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                            @Override
                            public long extractTimestamp(Event event, long l) {
                                return event.timestamp;
                            }
                        }));

        // 合流 - 流数据类型要求一致 
        source1.union(source2).process(new ProcessFunction<Event, String>() {
            @Override
            public void processElement(Event value, ProcessFunction<Event, String>.Context ctx, Collector<String> out)
                    throws Exception {
                // 时间戳转换成时间
                String watermark = TimeUtil.toString(ctx.timerService().currentWatermark());
                // 水位线：以最慢为准；默认每200ms检查一次水位线
                out.collect(value.toString() + " | 水 位 线 ： " + watermark);
            }
        }).print();

        env.execute();
    }
}
