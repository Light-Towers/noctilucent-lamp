package com.study.javaclient.test;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.study.javaclient.po.Product;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;

import java.io.IOException;

/**
 * 参考：Elasticsearch：使用最新的 Elasticsearch Java client 8.0 来创建索引并搜索
 *  https://juejin.cn/post/7080726607043756045
 */
public class SpringDataESIndexTest {

    private static ElasticsearchClient client;
    private static ElasticsearchAsyncClient asyncClient;

    public static synchronized void makeConnection() throws IOException {
        // 创建低级客户端
        // Create the low-level client
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "password"));


        RestClient restClient = RestClient.builder(new HttpHost("centos100", 9200))
                .setHttpClientConfigCallback(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .build();

        // 使用Jackson映射器创建传输(transport)
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        // 创建API客户端
        client = new ElasticsearchClient(transport);
        asyncClient = new ElasticsearchAsyncClient(transport);

        // 关闭ES客户端
//        transport.close();
//        restClient.close();
    }

    public static void main(String[] args) throws IOException {
        makeConnection();
        // Index data to an index products
        Product product = new Product("abc", "Bag", 42);
        IndexRequest<Object> indexRequest = new IndexRequest.Builder<>()
                .index("products")
                .id("abc")
                .document(product)
                .build();
        client.index(indexRequest);

        Product product1 = new Product("efg", "Bag", 42);
        client.index(builder -> builder
                .index("products")
                .id(product1.getId())
                .document(product1)
        );
        // Search for a data
        TermQuery query = QueryBuilders.term()
                .field("name")
                .value("bag")
                .build();
        SearchRequest request = new SearchRequest.Builder()
                .index("products")
                .query(query._toQuery())
                .build();
        SearchResponse<Product> search = client.search(request, Product.class);
        for (Hit<Product> hit : search.hits().hits()) {
            Product pd = hit.source();
            System.out.println(pd);
        }
        // The second search way
        SearchResponse<Product> search1 = client.search(s -> s
                        .index("products")
                        .query(q -> q
                                .term(t -> t
                                        .field("name")
                                        .value(v -> v.stringValue("bag"))
                                )),
                Product.class);
        for (Hit<Product> hit : search1.hits().hits()) {
            Product pd = hit.source();
            System.out.println(pd);
        }

        // Splitting complex DSL
        TermQuery termQuery = TermQuery.of(t -> t.field("name").value("bag"));
        SearchResponse<Product> search2 = client.search(s -> s
                        .index("products")
                        .query(termQuery._toQuery()),
                Product.class
        );
        for (Hit<Product> hit : search2.hits().hits()) {
            Product pd = hit.source();
            System.out.println(pd);
        }
        // Creating aggregations
        SearchResponse<Void> search3 = client.search(b -> b
                        .index("products")
                        .size(0)
                        .aggregations("price-histo", a -> a
                                .histogram(h -> h
                                        .field("price")
                                        .interval(20.0)
                                )
                        ),
                Void.class
        );
        long firstBucketCount = search3.aggregations()
                .get("price-histo")
                .histogram()
                .buckets().array()
                .get(0)
                .docCount();
        System.out.println("doc count: " + firstBucketCount);
    }

}
