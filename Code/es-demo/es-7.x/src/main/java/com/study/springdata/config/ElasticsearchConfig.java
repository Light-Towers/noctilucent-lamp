package com.study.springdata.config;

import lombok.Data;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {
    private String host ;
    private Integer port ;

    @Bean
    @Override
    public RestHighLevelClient elasticsearchClient() {

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port))
                // 异步httpclient配置
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    // 账号密码登录
//                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    // httpclient连接数配置
                    httpClientBuilder.setMaxConnTotal(30);
                    httpClientBuilder.setMaxConnPerRoute(10);
                    // httpclient保活策略
                    httpClientBuilder.setKeepAliveStrategy(((response, context) -> Duration.ofMinutes(5).toMillis()));
                    return httpClientBuilder;
                });

        return new RestHighLevelClient(builder);
    }

    @Bean
    public ElasticsearchRestTemplate elasticsearchRestTemplate(RestHighLevelClient elasticsearchClient, ElasticsearchConverter elasticsearchConverter){
        return new ElasticsearchRestTemplate(elasticsearchClient,elasticsearchConverter);
    }

}
