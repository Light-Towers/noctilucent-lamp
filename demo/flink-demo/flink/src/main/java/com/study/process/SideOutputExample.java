package com.study.process;

import com.alibaba.fastjson.JSON;
import com.study.base.Event;
import com.study.source.ClickSource;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

/**
 * 旁路输出(测输出流)
 * 官网参考: https://nightlies.apache.org/flink/flink-docs-master/zh/docs/dev/datastream/side_output/
 * @author osmondy
 */
public class SideOutputExample {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 这需要是一个匿名的内部类，以便我们分析类型
        OutputTag<String> outputTag = new OutputTag<String>("side-output"){};

        SingleOutputStreamOperator<String> mainDataStream = env.addSource(new ClickSource()).process(new ProcessFunction<Event, String>() {
            @Override
            public void processElement(Event value, Context ctx, Collector<String> out) throws Exception {
                // 发送数据到主流
                out.collect(JSON.toJSONString(value));
                // 水位线
                // System.out.println(ctx.timerService().currentWatermark());

                // 发送数据到旁路输出
                ctx.output(outputTag, "side-" + value.timestamp);
            }
        });
        // 从主流中获取测流
        DataStream<String> sideOutputStream = mainDataStream.getSideOutput(outputTag);

        // 主流输出
        mainDataStream.print();
        // 测流输出
        sideOutputStream.print();

        env.execute();
    }
}
