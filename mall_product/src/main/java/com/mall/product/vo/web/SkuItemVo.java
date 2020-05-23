package com.mall.product.vo.web;

import com.mall.product.entity.SkuImagesEntity;
import com.mall.product.entity.SkuInfoEntity;
import com.mall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {
    /**
     * Sku信息
     */
    SkuInfoEntity info;

    /**
     * 是否有有货
     */
    boolean hasStock;

    /**
     * Sku图片
     */
    List<SkuImagesEntity> images;

    /**
     * Spu销售属性信息
     */
    List<SkuItemSaleAttrVo> saleAttrs;

    /**
     * Spu规格参数
     */
    List<SpuItemAttrGroupVo> groupAttrs;

    /**
     * 商品详情
     */
    SpuInfoDescEntity desc;
}
