package com.study.process.TopN;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import com.study.base.Event;
import com.study.source.ClickSource;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * 统计10s窗口的url点击数；每5s计算一次
 * 方案一：WindowAll，只开一个窗口
 * 
 * @author osmondy
 */
public class ProcessAllWindowTopN {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> eventStream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forMonotonousTimestamps()
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                    @Override
                                    public long extractTimestamp(Event element, long recordTimestamp) {
                                        return element.timestamp;
                                    }
                                }));

        // 只需要url就可以统计数量，所以转换成String直接开窗统计
        SingleOutputStreamOperator<String> result = eventStream
                .map(new MapFunction<Event, String>() {
                    @Override
                    public String map(Event value) throws Exception {
                        return value.url;
                    }
                })
                .windowAll(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .process(new ProcessAllWindowFunction<String, String, TimeWindow>() {
                    @Override
                    public void process(ProcessAllWindowFunction<String, String, TimeWindow>.Context context,
                            Iterable<String> elements, Collector<String> out) throws Exception {
                        // 遍历窗口中数据，将浏览量保存到一个HashMap中
                        HashMap<String, Long> urlCountMap = new HashMap<>();
                        for (String url : elements) {
                            if (urlCountMap.containsKey(url)) {
                                urlCountMap.put(url, urlCountMap.get(url) + 1);
                            } else {
                                urlCountMap.put(url, 1L);
                            }
                        }

                        // 将浏览量数据放入ArrayList，进行排序
                        // 1. 先转成List
                        ArrayList<Tuple2<String, Long>> mapList = new ArrayList<>();
                        for (String key : urlCountMap.keySet()) {
                            mapList.add(Tuple2.of(key, urlCountMap.get(key)));
                        }
                        // 2.排序
                        mapList.sort(new Comparator<Tuple2<String, Long>>() {
                            @Override
                            public int compare(Tuple2<String, Long> o1, Tuple2<String, Long> o2) {
                                return (int) (o2.f1 - o1.f1);
                            }
                        });

                        // 取排序后的前两名，构建输出结果
                        StringBuilder result = new StringBuilder();
                        result.append("========================================\n");
                        for (int i = 0; i < 2; i++) {
                            Tuple2<String, Long> temp = mapList.get(i);
                            String info = "浏览量No." + (i + 1) +
                                    " url：" + temp.f0 +
                                    " 浏览量：" + temp.f1 +
                                    " 窗口结束时间：" + new Timestamp(context.window().getEnd()) + "\n";
                            result.append(info);
                        }
                        result.append("========================================\n");
                        out.collect(result.toString());
                    }
                });

        result.print();
        env.execute();
    }
}
