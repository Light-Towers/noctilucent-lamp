package com.study.cdc;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.hbase.sink.HBaseMutationConverter;
import org.apache.flink.connector.hbase.sink.HBaseSinkFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.BufferedMutator;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Mutation;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Flink_CDC {
    private static final Logger log = LoggerFactory.getLogger(Flink_CDC.class);

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("172.16.9.89")
                .port(3306)
                .databaseList("xyqb_user","kdsp")
                .tableList("xyqb_user.user","kdsp.t_order_info")
                .username("root")
                .password("123456")
                .startupOptions(StartupOptions.initial())
                .scanNewlyAddedTableEnabled(true)
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();


        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.master", "172.16.9.90:16000");
        configuration.set("hbase.zookeeper.quorum", "172.16.9.89");
        configuration.set("hbase.zookeeper.property.clientPort", "2181");

        // 构造 Hbase 转换器
        HBaseMutationConverter<String> hBaseMutationConverter = new HBaseMutationConverter<String>() {
            @Override
            public void open() {
                BufferedMutator mutator;
            }

            @Override
            public Mutation convertToMutation(String value) {
//                System.out.println("【数据】" + value);
                String familyName;
                Mutation mutation = null;
                try {
                    JSONObject jsonObject = JSON.parseObject(value);
                    // 设值列族
                    familyName = jsonObject.getJSONObject("source").getString("table");
                    // 1.读取操作 "c":增 ｜ "u":改 ｜ "d":删 ｜ "r":查
                    String op = jsonObject.getString("op");
                    /** 处理 "xyqb_user.user","kdsp.t_order_info" 数据，变成维表 **/

                    if ("d".equals(op)) {
                        // 删数据
                        JSONObject before = jsonObject.getJSONObject("before");
                        String rowKey = getRowKey(before, familyName);
                        mutation = new Delete(Bytes.toBytes(rowKey));
                    } else {
                        // TODO 还需要考虑修改数据、修改字段的问题
                        // 其余统一作更改
                        JSONObject after = jsonObject.getJSONObject("after");
                        // 遍历key集合
                        String rowKey = getRowKey(after, familyName);
                        Put put = new Put(Bytes.toBytes(rowKey));
                        for (String key : after.keySet()) {
                            // 对null值的处理
                            if (after.getString(key) != null){
                                put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(key), Bytes.toBytes(after.getString(key)));
                            }
                        }
                        mutation = put;
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                return mutation;
            }
        };

        HBaseSinkFunction<String> hBaseSinkFunction = new HBaseSinkFunction<>("bigdata:dimension1", configuration, hBaseMutationConverter, 1024 * 1024 * 5, 1000, 3);


        //执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(2);
        // enable checkpoint
        env.enableCheckpointing(3000);
        DataStreamSource<String> sqlSource = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");
        // 打印
        sqlSource.print().setParallelism(1);
        sqlSource.addSink(hBaseSinkFunction).name("Hbase Sink");

        System.out.println("-----开始----");
        env.execute("Mysql To Hbase");
    }

    static String getRowKey(JSONObject jsonObject, String familyName) {
        String rowKey;
        // 根据表设置rowkey，此处我们用userId作为主键
        if ("user".equals(familyName)){
            rowKey = jsonObject.getString("id");
        }else {
            rowKey = jsonObject.getString("user_id");
        }
        return rowKey;
    }
}
