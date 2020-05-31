package com.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.ware.entity.WareSkuEntity;
import com.mall.ware.vo.SkuHasStockVo;
import com.mall.ware.vo.SkuStockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:44:05
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCondition(Map<String, Object> params);

    void addStock(Long wareId, Long skuId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStock(List<Long> skuId);

    Boolean lockStock(List<SkuStockVo> skuStockVos);
}

