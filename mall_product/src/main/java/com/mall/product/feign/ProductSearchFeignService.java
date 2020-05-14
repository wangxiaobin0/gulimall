package com.mall.product.feign;

import com.mall.common.to.SkuEsModel;
import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.util.List;

@FeignClient("mall-search")
public interface ProductSearchFeignService {
    @PostMapping("/search/product")
    R save(@RequestBody List<SkuEsModel> skuEsModelList) throws IOException;
}
