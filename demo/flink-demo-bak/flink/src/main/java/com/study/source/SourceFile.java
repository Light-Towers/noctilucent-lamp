package com.study.source;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * 流处理
 * 
 * @author osmondy
 */
public class SourceFile {
    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置并行度
        env.setParallelism(4);

        // 1、从文件中读取文件数据
        String inputPath = "/Users/yangjinhua/Research/flink-demo/flink/src/main/resources/hello.txt";
        DataStream<String> inputDataStream = env.readTextFile(inputPath);
        // 从分布式文件系统中读取
        // DataStream<String> inputDataStream = env.readTextFile("hdfs:///resources/hello.txt");

        // 对数据流进行处理，按空格分词展开，转换成（word，1）二元组进行统计
        DataStream<Tuple2<String, Integer>> outputStream = inputDataStream
                .flatMap(new MyFlatMap()).setParallelism(3)
                // 按照第一个位置word进行分组
                .keyBy(0)
                // 按照第二个位置数据求和
                .sum(1).setParallelism(2);

        outputStream.print();

        // 执行任务--流式处理必备
        env.execute();
    }

    // 自定义FlatMap实现 FlatMapFunction
    public static class MyFlatMap implements FlatMapFunction<String, Tuple2<String, Integer>> {

        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> out) throws Exception {
            // 按空格分词
            String[] words = value.split(" ");
            // 遍历所有的word，包成二元组
            for (String word : words) {
                out.collect(new Tuple2<>(word, 1));
            }
        }
    }

}
