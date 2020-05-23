package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.vo.web.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:31
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkuListBySpuId(Long spuId);

    SkuItemVo getItemInfo(Long skuId);
}

