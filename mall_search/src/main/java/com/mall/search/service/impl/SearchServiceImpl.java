package com.mall.search.service.impl;

import com.mall.search.constant.EsConstant;
import com.mall.search.service.ISearchService;
import com.mall.search.vo.SearchParam;
import com.mall.search.vo.SearchResponse;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

@Service
public class SearchServiceImpl implements ISearchService {

    @Autowired
    RestHighLevelClient client;
    @Override
    public SearchResponse search(SearchParam searchParam) throws IOException {

        SearchRequest searchRequest = createSearchRequest(searchParam);

        client.search(searchRequest, RequestOptions.DEFAULT);


        return null;
    }

    private SearchRequest createSearchRequest(SearchParam searchParam) {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder builder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //按关键字查询
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        //按分类id过滤
        if (searchParam.getCatelog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("categoryId", searchParam.getCatelog3Id()));
        }
        //按品牌过滤
        if (searchParam.getBrandId() != null && !searchParam.getBrandId().isEmpty()) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        //按是否有货过滤
        if (searchParam.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }

        //按价格区间过滤
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String skuPrice = searchParam.getSkuPrice();
            String[] strings = skuPrice.split("_");
            //1_500形式
            if (strings.length == 2) {
                rangeQuery.gte(strings[0]).lte(strings[1]);
            } else if (skuPrice.startsWith("_")) {
                rangeQuery.lte(strings[0]);
            } else if (skuPrice.endsWith("_")) {
                rangeQuery.gte(strings[0]);
            }
            boolQuery.filter(rangeQuery);
        }

        if (searchParam.getAttrs() != null && !searchParam.getAttrs().isEmpty()) {
            List<String> attrs = searchParam.getAttrs();
            for (String attr : attrs) {
                BoolQueryBuilder query = QueryBuilders.boolQuery();
                String[] strings = attr.split("_");
                query.must(QueryBuilders.termQuery("attrs.attrId", strings[0]));
                query.must(QueryBuilders.termsQuery("attrs.attrValue", strings[1].split(":")));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", query, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }
        builder.query(boolQuery);

        //排序
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String[] strings = searchParam.getSort().split("_");
            SortOrder sortOrder = strings[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            builder.sort(strings[0], sortOrder);
        }

        //分页
        builder.from((searchParam.getPageNum() - 1) *EsConstant.ES_PRODUCT_PAGE_SIZE);
        builder.size(EsConstant.ES_PRODUCT_PAGE_SIZE);

        //高亮
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }

        String s = builder.toString();
        System.out.println(s);

        searchRequest.source(builder);
        return searchRequest;
    }
}
