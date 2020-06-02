package com.mall.product.feign;

import com.mall.product.vo.SeckillSkuRedisTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient("mall-seckill")
public interface SeckillServiceFeign {
    @GetMapping("/{skuId}")
    SeckillSkuRedisTo secInfo(@PathVariable("skuId") Long skuId);
}
