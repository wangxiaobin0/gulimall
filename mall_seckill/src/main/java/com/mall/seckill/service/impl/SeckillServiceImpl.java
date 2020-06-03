package com.mall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mall.common.constrant.OrderMQConstant;
import com.mall.common.to.SeckillOrderTo;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberEntity;
import com.mall.seckill.feign.CouponServiceFeign;
import com.mall.seckill.feign.ProductServiceFeign;
import com.mall.seckill.interceptor.SeckillRequestInterceptor;
import com.mall.seckill.service.SeckillService;
import com.mall.seckill.to.SeckillSessionTo;
import com.mall.seckill.to.SeckillSkuRedisTo;
import com.mall.seckill.vo.SeckillSkuRelationVo;
import com.mall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.util.IdUtils;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class SeckillServiceImpl implements SeckillService {

    public static final String SECKILL_INFO = "seckill:sku:";
    public static final String SECKILL_SESSION_INFO = "seckill:session:";
    public static final String SECKILL_STOCK_SEMAPHORE = "seckill:stock:";


    @Autowired
    CouponServiceFeign couponServiceFeign;

    @Autowired
    ProductServiceFeign productServiceFeign;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    SeckillRequestInterceptor seckillRequestInterceptor;

    @Override
    public void upTomorrowSeckill() {
        //1.场次信息。startTime_endTime:sessionId_skuId 20200601_20200602:1_1
        R seckillInfo = couponServiceFeign.getTomorrowSeckillInfo();
        Integer code = seckillInfo.get("code", Integer.class);
        if (code == 0) {
            List<SeckillSessionTo> seckillSession = seckillInfo.get("seckillSession", new TypeReference<List<SeckillSessionTo>>() {
            });
            saveSeckillSkuInfo(seckillSession);
        }
    }

    @Override
    public List<SeckillSkuRedisTo> todaySeckill() {
        Long current = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SECKILL_SESSION_INFO + "*");
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                key = key.replace(SECKILL_SESSION_INFO, "");
                Long startTime = Long.parseLong(key.split("_")[0]);
                Long endTime = Long.parseLong(key.split("_")[1]);
                if (startTime <= current && current <= endTime) {
                    List<String> range = redisTemplate.opsForList().range(SECKILL_SESSION_INFO + key, 0, -1);
                    BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_INFO);
                    List<String> strings = hashOps.multiGet(range);
                    if (!StringUtils.isEmpty(strings)) {
                        List<SeckillSkuRedisTo> redisTos = strings.stream().map(item -> {
                            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(item, SeckillSkuRedisTo.class);
                            skuRedisTo.setToken(null);
                            return skuRedisTo;
                        }).collect(Collectors.toList());
                        return redisTos;
                    }

                }
            }
        }
        return null;
    }

    @Override
    public SeckillSkuRedisTo secInfo(Long skuId) {
        Long sessionId = couponServiceFeign.getSecKillInfoBySkuId(skuId);
        if (sessionId != null) {
            BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_INFO);
            String s = hashOps.get(sessionId + "_" + skuId);
            SeckillSkuRedisTo skuRedisTo = JSON.parseObject(s, SeckillSkuRedisTo.class);
            return skuRedisTo;
        }
        return null;
    }

    @Override
    public String createSeckillOrder(Long skuId, Long sessionId, String token, Integer num) {

        //1. 验证是否登录,由拦截器校验
        //2. 验证商品是否参加秒杀活动
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_INFO);
        String sessionKey = sessionId + "_" + skuId;
        //商品参加秒杀
        if (hashOps.hasKey(sessionKey)) {
            String skuJson = hashOps.get(sessionKey);
            com.mall.common.to.SeckillSkuRedisTo redisTo = JSON.parseObject(skuJson, com.mall.common.to.SeckillSkuRedisTo.class);
            if (token.equals(redisTo.getToken())) {
                if (num < redisTo.getSeckillCount().intValue()) {
                    MemberEntity loginUser = seckillRequestInterceptor.getLoginUser();
                    //秒杀过商品的用户的key
                    String seckillKey = sessionKey + "_" + loginUser.getId();
                    if (!redisTemplate.hasKey(seckillKey)) {
                        RSemaphore semaphore = redissonClient.getSemaphore(SECKILL_STOCK_SEMAPHORE + token);
                        boolean b = semaphore.tryAcquire(num);
                        //发送
                        if (b) {
                            SeckillOrderTo orderTo = new SeckillOrderTo();
                            orderTo.setMemberId(loginUser.getId());
                            orderTo.setOrderSn(UUID.randomUUID().toString());
                            orderTo.setRedisTo(redisTo);
                            orderTo.setNum(num);
                            rabbitTemplate.convertAndSend(OrderMQConstant.ORDER_EXCHANGE, OrderMQConstant.ORDER_SECKILL_ROUTING_KEY, orderTo);
                            //记录此账户已参加过活动
                            redisTemplate.opsForValue().set(seckillKey, num.toString());
                            //TODO:发送延时消息，主动判断订单状态，判断是否需要补偿库存

                            return orderTo.getOrderSn();
                        } else {
                            throw new RuntimeException("库存不足");
                        }
                    } else {
                        throw new RuntimeException("每位用户只能参加一次秒杀活动");
                    }

                } else {
                    throw new RuntimeException("购买数量超出限制");
                }
            } else {
                throw new RuntimeException("秒杀商品token异常，刷新页面后重试");
            }
        } else {
            throw new RuntimeException("该商品为参加秒杀活动");
        }
    }

    /**
     * 活动sku信息：
     * hash结构
     * key=seckill:sku
     * field=sessionId_skuId,   使用sessionId+skuId保证，不同活动中的同一个sku在不同价格下的冲突
     * value=SeckillRedisTo
     * 活动信息
     * list结构
     * key=seckill:session:startTime_endTime
     * value=List<sessionId_skuId> 方便直接查询sku信息
     * 信号量，也就是库存
     * key:SECKILL_STOCK_SEMAPHORE:token   使用token，避免在秒杀活动开始前就被锁定库存，token在活动开始后才会暴露给客户
     * value:库存
     *
     * @param seckillSession
     */
    private void saveSeckillSkuInfo(List<SeckillSessionTo> seckillSession) {
        if (seckillSession != null && !seckillSession.isEmpty()) {
            for (SeckillSessionTo sessionTo : seckillSession) {
                BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(SECKILL_INFO);
                //保存sku信息. key:filed:value= seckill.sku:sessionId_skuId:value
                for (SeckillSkuRelationVo sku : sessionTo.getRelationSkus()) {
                    String token = UUID.randomUUID().toString();
                    String key = sessionTo.getId() + "_" + sku.getSkuId();
                    if (!hashOps.hasKey(key)) {
                        SeckillSkuRedisTo to = new SeckillSkuRedisTo();
                        //秒杀活动的信息
                        BeanUtils.copyProperties(sku, to);
                        //查询sku信息
                        R info = productServiceFeign.info(sku.getSkuId());
                        if (info.get("code", Integer.class) != 0) {
                            throw new RuntimeException("查询商品" + sku.getSkuId() + "信息失败， 秒杀商品上架失败");
                        }
                        SkuInfoVo skuInfo = info.get("skuInfo", SkuInfoVo.class);
                        to.setSkuInfoVo(skuInfo);
                        //设置秒杀信息
                        to.setStartTime(sessionTo.getStartTime());
                        to.setEndTime(sessionTo.getEndTime());
                        //设置token
                        to.setToken(token);
                        hashOps.put(key, JSON.toJSONString(to));

                        //设置库存信号量做限流
                        String semaphoreKey = SECKILL_STOCK_SEMAPHORE + token;
                        RSemaphore semaphore = redissonClient.getSemaphore(semaphoreKey);
                        semaphore.trySetPermits(to.getSeckillCount().intValue());
                    }
                }
                List<String> sessionInfo = sessionTo.getRelationSkus().stream().map(to -> {
                    Long skuId = to.getSkuId();
                    Long sessionToId = sessionTo.getId();
                    String value = sessionToId + "_" + skuId;
                    return value;
                }).collect(Collectors.toList());
                String key = SECKILL_SESSION_INFO + sessionTo.getStartTime().getTime() + "_" + sessionTo.getEndTime().getTime();
                //保存活动场次信息
                if (!redisTemplate.hasKey(key)) {
                    redisTemplate.opsForList().leftPushAll(key, sessionInfo);
                }
            }

        }
    }
}
