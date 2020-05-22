package com.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.mall.common.to.SkuEsModel;
import com.mall.search.constant.EsConstant;
import com.mall.search.service.ISearchService;
import com.mall.search.vo.SearchParam;
import com.mall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements ISearchService {

    @Autowired
    RestHighLevelClient client;
    @Override
    public SearchResult search(SearchParam searchParam) throws IOException {

        SearchRequest searchRequest = createSearchRequest(searchParam);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchResult searchResult = createSearchResult(searchResponse, searchParam);


        return searchResult;
    }

    private SearchResult createSearchResult(SearchResponse searchResponse, SearchParam searchParam) {
        SearchResult result = new SearchResult();

        SearchHits hits = searchResponse.getHits();

        //属性
        List<SkuEsModel> products = new ArrayList<>();

        for (SearchHit hit : hits.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
            if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();
                skuEsModel.setSkuTitle(skuTitle);
            }
            products.add(skuEsModel);
        }
        result.setProducts(products);

        //分页
        result.setTotal(hits.getTotalHits().value);
        result.setTotalPage(hits.getTotalHits().value % EsConstant.ES_PRODUCT_PAGE_SIZE == 0 ? (int) hits.getTotalHits().value / EsConstant.ES_PRODUCT_PAGE_SIZE : (int)hits.getTotalHits().value / EsConstant.ES_PRODUCT_PAGE_SIZE + 1);
        result.setPageNum(searchParam.getPageNum());

        //分类
        List<SearchResult.BrandVo> brands = new ArrayList<>();
        ParsedLongTerms brand_agg = searchResponse.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            long brandId = Long.parseLong(bucket.getKeyAsString());
            ParsedStringTerms brand_name_agg = bucket.getAggregations().get("brand_name_agg");
            String brandName = brand_name_agg.getBuckets().get(0).getKeyAsString();
            ParsedStringTerms brand_img_agg = bucket.getAggregations().get("brand_img_agg");
            String brandImg = brand_img_agg.getBuckets().get(0).getKeyAsString();
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);
            brands.add(brandVo);
        }
        result.setBrands(brands);

        //分类
        List<SearchResult.CategoryVo> categories = new ArrayList<>();
        ParsedLongTerms category_agg = searchResponse.getAggregations().get("category_agg");
        for (Terms.Bucket bucket : category_agg.getBuckets()) {
            SearchResult.CategoryVo categoryVo = new SearchResult.CategoryVo();
            long cateId = Long.parseLong(bucket.getKeyAsString());
            ParsedStringTerms category_name_agg = bucket.getAggregations().get("category_name_agg");
            String cateName = category_name_agg.getBuckets().get(0).getKeyAsString();
            categoryVo.setCateId(cateId);
            categoryVo.setCateName(cateName);
            categories.add(categoryVo);
        }
        result.setCategories(categories);

        //attrs
        List<SearchResult.AttrVo> attrsList = new ArrayList<>();

        ParsedNested attrs_agg = searchResponse.getAggregations().get("attrs_agg");
        ParsedLongTerms attr_id_agg = attrs_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attr = new SearchResult.AttrVo();
            long attrId = Long.parseLong(bucket.getKeyAsString());
            ParsedStringTerms attr_name_agg = bucket.getAggregations().get("attr_name_agg");
            String attrName = attr_name_agg.getBuckets().get(0).getKeyAsString();
            ParsedStringTerms attr_value_agg = bucket.getAggregations().get("attr_value_agg");
            List<String> attrValue = attr_value_agg.getBuckets().stream().map(item -> item.getKeyAsString()).collect(Collectors.toList());
            attr.setAttrId(attrId);
            attr.setAttrName(attrName);
            attr.setAttrValue(attrValue);
            attrsList.add(attr);
        }
        result.setAttrs(attrsList);
        return result;
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


        //品牌id
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId");
        //品牌名称
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        //品牌图片
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        builder.aggregation(brand_agg);

        //分类id
        TermsAggregationBuilder category_agg = AggregationBuilders.terms("category_agg").field("categoryId");
        //分类名称
        category_agg.subAggregation(AggregationBuilders.terms("category_name_agg").field("categoryName").size(1));
        builder.aggregation(category_agg);

        //nested聚合
        NestedAggregationBuilder attrs_agg = AggregationBuilders.nested("attrs_agg", "attrs");
        //attr_id
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        //attr_name
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        //attr_value
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue"));
        attrs_agg.subAggregation(attr_id_agg);
        builder.aggregation(attrs_agg);

        System.out.println(builder.toString());

        searchRequest.source(builder);
        return searchRequest;
    }
}
