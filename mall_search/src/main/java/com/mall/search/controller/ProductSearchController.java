package com.mall.search.controller;

import com.mall.common.to.SkuEsModel;
import com.mall.common.utils.R;
import com.mall.search.service.ProductSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/product")
public class ProductSearchController {

    @Autowired
    ProductSearchService productSearchService;

    @PostMapping
    public R save(@RequestBody List<SkuEsModel> skuEsModelList) throws IOException {
        log.info("保存商品搜索信息：{}" , skuEsModelList);
        Boolean b = productSearchService.save(skuEsModelList);
        if (b) {
            return R.ok();
        } else {
            return R.error();
        }
    }
}
