package com.mall.cart.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(value = "mall-product")
public interface ProductServiceFeign {
    @RequestMapping("/product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);


    @GetMapping("/product/skusaleattrvalue/{skuId}")
    public List<String> getSkuAttr(@PathVariable("skuId") Long skuId);
}
