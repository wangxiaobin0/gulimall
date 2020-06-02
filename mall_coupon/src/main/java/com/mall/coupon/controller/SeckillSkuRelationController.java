package com.mall.coupon.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mall.coupon.entity.SeckillSessionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.mall.coupon.entity.SeckillSkuRelationEntity;
import com.mall.coupon.service.SeckillSkuRelationService;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.R;


/**
 * 秒杀活动商品关联
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:35:50
 */
@RestController
@RequestMapping("coupon/seckillskurelation")
public class SeckillSkuRelationController {
    @Autowired
    private SeckillSkuRelationService seckillSkuRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:seckillskurelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = seckillSkuRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:seckillskurelation:info")
    public R info(@PathVariable("id") Long id) {
        SeckillSkuRelationEntity seckillSkuRelation = seckillSkuRelationService.getById(id);

        return R.ok().put("seckillSkuRelation", seckillSkuRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:seckillskurelation:save")
    public R save(@RequestBody SeckillSkuRelationEntity seckillSkuRelation) {
        seckillSkuRelationService.save(seckillSkuRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:seckillskurelation:update")
    public R update(@RequestBody SeckillSkuRelationEntity seckillSkuRelation) {
        seckillSkuRelationService.updateById(seckillSkuRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:seckillskurelation:delete")
    public R delete(@RequestBody Long[] ids) {
        seckillSkuRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @GetMapping("/tomorrow")
    public R getTomorrowSeckillInfo() {
        List<SeckillSessionEntity> list = seckillSkuRelationService.getTomorrowSeckillInfo();
        return R.ok().put("seckillSession", list);
    }

    @GetMapping("/{skuId}")
    public Long getSecKillInfoBySkuId(@PathVariable("skuId") Long skuId) {
        Long sessionId = seckillSkuRelationService.getSecKillInfoBySkuId(skuId);
        return sessionId;
    }
}
