package com.mall.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.to.SkuEsModel;
import com.mall.search.constant.EsConstant;
import com.mall.search.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.util.SecretKeyUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class ProductSearchServiceImpl implements ProductSearchService {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    RestHighLevelClient restHighLevelClient;

    @Override
    public Boolean save(List<SkuEsModel> skuEsModelList) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModelList) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.ES_PRODUCT_INDEX).source(objectMapper.writeValueAsString(skuEsModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        boolean hasFailures = bulkResponse.hasFailures();
        if (!hasFailures) {
            log.info("成功录入：{},耗时：{}ms" , bulkResponse.toString(), bulkResponse.getTook());
            return true;
        } else {
            return false;
        }
    }
}
