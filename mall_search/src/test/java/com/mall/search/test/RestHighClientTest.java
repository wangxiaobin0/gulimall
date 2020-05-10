package com.mall.search.test;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;

/**
 * @author
 * @date 2020/5/10
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RestHighClientTest {

    @Autowired
    RestHighLevelClient client;

    @Test
    public void testIndex() throws IOException {
        IndexRequest indexRequest = new IndexRequest("user");
        indexRequest.id("1");
        User user = new User();
        user.setUsername("zhangsna");
        user.setAge(1);
        String s = JSON.toJSONString(user);
        indexRequest.source(s, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(index);
    }

    @Test
    public void query() throws IOException {
        GetRequest request = new GetRequest();
        request.index("user");
        request.id("1");
        GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
        System.out.println(documentFields);
        request.index("user");
        request.id("2");
        GetResponse documentFields2 = client.get(request, RequestOptions.DEFAULT);
        System.out.println(documentFields2);

    }

    @Test
    public void fuza() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //按地址关键字进行查询
        builder.query(QueryBuilders.matchQuery("address", "Street"));

        //按性别划分
        TermsAggregationBuilder city = AggregationBuilders.terms("city").field("city.keyword");
        AvgAggregationBuilder balance = AggregationBuilders.avg("balance").field("balance");
        builder.aggregation(city);
        builder.aggregation(balance);
        builder.size(5);

        log.info("请求条件为：个数{}" ,builder.size());
        searchRequest.source(builder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        log.info("搜索结果为：{}", searchResponse);
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            System.out.println(hit.getSourceAsString());
        }
        Aggregations aggregations = searchResponse.getAggregations();
        Terms city1 = aggregations.get("city");
        city1.getBuckets().forEach(item -> {
            System.out.println(item.getKeyAsString() + ":" + item.getDocCount());
        });
        Avg balance1 = aggregations.get("balance");
        System.out.println("avg balance:" + balance1.getValue());
    }

}

@Data
class User {
    private String username;
    private Integer age;
}
