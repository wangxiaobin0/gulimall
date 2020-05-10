package com.mall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author
 * @date 2020/5/10
 */
@Configuration
public class ElasticSearchConfig {

    @Bean
    RestHighLevelClient elasticClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost("112.126.59.115", 9200, "http"));
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
