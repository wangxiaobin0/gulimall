package com.mall.seckill.controller;

import com.mall.seckill.service.SeckillService;
import com.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping
    public List<SeckillSkuRedisTo> todaySeckill() {
        List<SeckillSkuRedisTo> to = seckillService.todaySeckill();
        return to;
    }

    @GetMapping("/{skuId}")
    public SeckillSkuRedisTo secInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo seckillSkuRedisTo = seckillService.secInfo(skuId);
        return seckillSkuRedisTo;
    }
}
