package com.study.sink;

import com.study.base.Event;
import org.apache.flink.connector.elasticsearch.sink.Elasticsearch7SinkBuilder;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import java.util.HashMap;
import java.util.Map;

/**
 * @author osmondy
 */
public class SinkEs {
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

        stream.sinkTo(
            new Elasticsearch7SinkBuilder<Event>()
                // 下面的设置使 sink 在接收每个元素之后立即提交，否则这些元素将被缓存起来
                .setBulkFlushMaxActions(1)
                .setHosts(new HttpHost("172.16.9.89", 9200, "http"), new HttpHost("172.16.9.89", 9201, "http"), new HttpHost("172.16.9.89", 9202, "http"))
                .setConnectionUsername("elastic")
                .setConnectionPassword("123456")
                .setEmitter(
                (element, context, indexer) ->
                indexer.add(createIndexRequest(element)))
                .build());

        env.execute();
    }

    private static IndexRequest createIndexRequest(Event event) {
        Map<String, String> json = new HashMap<>();
        json.put(event.user, event.url);
    
        return Requests.indexRequest()
            .index("my-index")
            // .id(element)
            .source(json);
    }
}