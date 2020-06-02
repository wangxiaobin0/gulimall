package com.mall.seckill.feign;

import com.mall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mall-coupon")
public interface CouponServiceFeign {

    @GetMapping("/coupon/seckillskurelation/tomorrow")
    R getTomorrowSeckillInfo();

    @GetMapping("/coupon/seckillskurelation/{skuId}")
    Long getSecKillInfoBySkuId(@PathVariable("skuId") Long skuId);
}
