package com.mall.coupon.service.impl;

import com.mall.common.to.MemberPrice;
import com.mall.common.to.SkuReductionTo;
import com.mall.coupon.entity.MemberPriceEntity;
import com.mall.coupon.entity.SkuLadderEntity;
import com.mall.coupon.service.MemberPriceService;
import com.mall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.coupon.dao.SkuFullReductionDao;
import com.mall.coupon.entity.SkuFullReductionEntity;
import com.mall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {
    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1. 保存阶梯价格
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if (skuLadderEntity.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        //2.保存满减打折会员价
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());
        skuFullReductionEntity.setSkuId(skuReductionTo.getSkuId());
        if (skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal(0)) == 1) {
            this.save(skuFullReductionEntity);
        }

        //3.保存会员价格
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> priceEntities = memberPrice.stream().map(price -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            memberPriceEntity.setMemberLevelId(price.getId());
            memberPriceEntity.setMemberLevelName(price.getName());
            memberPriceEntity.setMemberPrice(price.getPrice());
            memberPriceEntity.setAddOther(1);
            return memberPriceEntity;
        }).filter(entity-> {
            return entity.getMemberPrice().compareTo(new BigDecimal(0)) == 1;
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(priceEntities);
    }

}