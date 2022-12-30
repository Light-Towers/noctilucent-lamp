package com.study.process.TopN;

import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import com.study.base.Event;
import com.study.base.UrlViewCount;
import com.study.source.ClickSource;

import java.sql.Timestamp;
import java.util.ArrayList;

/**
 * 统计10s窗口的url点击数；每5s计算一次
 * 方案二：根据key开窗口
 * 
 * @author osmondy
 */
public class KeyedProcessTopN {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 从自定义数据源读取数据
        SingleOutputStreamOperator<Event> eventStream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps()
                        .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                            @Override
                            public long extractTimestamp(Event element, long recordTimestamp) {
                                return element.timestamp;
                            }
                        }));

        // 需要按照url分组，求出每个url的访问量
        SingleOutputStreamOperator<UrlViewCount> urlCountStream = eventStream
                .keyBy(data -> data.url)
                .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .aggregate(new UrlViewCountAgg(), new UrlViewCountResult());

        // 对结果中同一个窗口的统计数据，进行排序处理
        SingleOutputStreamOperator<String> result = urlCountStream
                .keyBy(data -> data.windowEnd)
                .process(new TopN(2));

        result.print("result");

        env.execute();
    }

    // 自定义增量聚合
    public static class UrlViewCountAgg implements AggregateFunction<Event, Long, Long> {
        @Override
        public Long createAccumulator() {
            return 0L;
        }

        @Override
        public Long add(Event value, Long accumulator) {
            return accumulator + 1;
        }

        @Override
        public Long getResult(Long accumulator) {
            return accumulator;
        }

        @Override
        public Long merge(Long a, Long b) {
            return null;
        }
    }

    // 自定义全窗口函数，只需要包装窗口信息
    public static class UrlViewCountResult extends ProcessWindowFunction<Long, UrlViewCount, String, TimeWindow> {

        @Override
        public void process(String url, Context context, Iterable<Long> elements, Collector<UrlViewCount> out)
                throws Exception {
            // 结合窗口信息，包装输出内容
            Long start = context.window().getStart();
            Long end = context.window().getEnd();
            // System.out.println("---ProcessWindowFunction--- currentProcessingTime: " + context.currentProcessingTime() + " Watermark: " + context.currentWatermark());
            out.collect(new UrlViewCount(url, elements.iterator().next(), start, end));
        }
    }

    // 自定义处理函数，排序取top n
    public static class TopN extends KeyedProcessFunction<Long, UrlViewCount, String> {
        // 将n作为属性
        private Integer n;
        // 定义一个列表状态
        private ListState<UrlViewCount> urlViewCountListState;

        public TopN(Integer n) {
            this.n = n;
        }

        @Override
        public void open(Configuration parameters) throws Exception {
            // System.out.println("111111 -- begin");
            // 从环境中获取列表状态句柄
            urlViewCountListState = getRuntimeContext().getListState(
                    new ListStateDescriptor<UrlViewCount>("url-view-count-list", Types.POJO(UrlViewCount.class)));
            
            // System.out.println("111111" + JSON.toJSONString(urlViewCountListState));
            // System.out.println("111111 -- end");
        }

        @Override
        public void processElement(UrlViewCount value, Context ctx, Collector<String> out) throws Exception {
            // System.out.println("2222222 -- begin");
            // System.out.println("++++KeyedProcessFunction++++ currentProcessingTime: ");
            // 将count数据添加到列表状态中，保存起来
            urlViewCountListState.add(value);
            // 注册 window end + 1ms后的定时器，等待所有数据到齐开始排序
            ctx.timerService().registerEventTimeTimer(ctx.getCurrentKey() + 1);
            // System.out.println("2222222 -- end");
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
            // System.out.println("333333 -- begin");
            // System.out.println("333333" + JSON.toJSONString(urlViewCountListState.get()));
            // 将数据从列表状态变量中取出，放入ArrayList，方便排序
            ArrayList<UrlViewCount> urlViewCountArrayList = new ArrayList<>();
            for (UrlViewCount urlViewCount : urlViewCountListState.get()) {
                urlViewCountArrayList.add(urlViewCount);
            }
            // 清空状态，释放资源
            urlViewCountListState.clear();

            // 排序
            urlViewCountArrayList.sort((o1, o2) -> o2.count.intValue() - o1.count.intValue());

            // 取前两名，构建输出结果
            StringBuilder result = new StringBuilder();
            result.append("========================================\n");
            result.append("窗口结束时间：" + new Timestamp(timestamp - 1) + "\n");
            for (int i = 0; i < Math.min(this.n, urlViewCountArrayList.size()); i++) {
                UrlViewCount urlViewCount = urlViewCountArrayList.get(i);
                String info = "No." + (i + 1) + " "
                        + "url：" + urlViewCount.url + " "
                        + "浏览量：" + urlViewCount.count + "\n";
                result.append(info);
            }
            result.append("========================================\n");
            out.collect(result.toString());
            // System.out.println("333333 -- end \n\n");
        }
    }
}
