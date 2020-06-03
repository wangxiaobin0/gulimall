package com.mall.seckill.service;


import com.mall.seckill.to.SeckillSkuRedisTo;

import java.util.List;


public interface SeckillService {
    void upTomorrowSeckill();

    List<SeckillSkuRedisTo> todaySeckill();

    SeckillSkuRedisTo secInfo(Long skuId);

    String createSeckillOrder(Long skuId, Long sessionId, String token, Integer num);
}
