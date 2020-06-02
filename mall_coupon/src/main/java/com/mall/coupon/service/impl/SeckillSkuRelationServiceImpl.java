package com.mall.coupon.service.impl;

import com.mall.coupon.entity.SeckillSessionEntity;
import com.mall.coupon.service.SeckillSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.coupon.dao.SeckillSkuRelationDao;
import com.mall.coupon.entity.SeckillSkuRelationEntity;
import com.mall.coupon.service.SeckillSkuRelationService;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Autowired
    SeckillSessionService seckillSessionService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                new QueryWrapper<SeckillSkuRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Async
    public List<SeckillSessionEntity> getTomorrowSeckillInfo() {

        QueryWrapper<SeckillSessionEntity> queryWrapper = new QueryWrapper<>();
        String startTime = startTime();
        String endTime = endTime();
        queryWrapper.between("start_time", startTime, endTime);
        List<SeckillSessionEntity> sessionEntities = seckillSessionService.list(queryWrapper);
        if (sessionEntities != null && !sessionEntities.isEmpty()) {
            List<SeckillSessionEntity> entities = sessionEntities.stream().map(seckillSessionEntity -> {
                List<SeckillSkuRelationEntity> relationSkus = this.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", seckillSessionEntity.getId()));
                seckillSessionEntity.setRelationSkus(relationSkus);
                return seckillSessionEntity;
            }).collect(Collectors.toList());
            return entities;
        }
        return null;
    }

    @Override
    public Long getSecKillInfoBySkuId(Long skuId) {
        List<SeckillSkuRelationEntity> entities = this.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("sku_id", skuId));
        if (entities != null && !entities.isEmpty()) {
            return entities.get(0).getPromotionSessionId();
        }
        return null;
    }


    private String startTime() {
        LocalDate startTime = LocalDate.now().plusDays(1);
        LocalDateTime of = LocalDateTime.of(startTime, LocalTime.MIN);
        String format = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }
    private String endTime() {
        LocalDate endTime = LocalDate.now().plusDays(1);
        LocalDateTime of = LocalDateTime.of(endTime, LocalTime.MAX);
        String format = of.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return format;
    }
}