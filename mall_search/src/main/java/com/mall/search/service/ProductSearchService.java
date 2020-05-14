package com.mall.search.service;

import com.mall.common.to.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSearchService {
    Boolean save(List<SkuEsModel> skuEsModelList) throws IOException;
}
