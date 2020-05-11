package com.mall.product.vo;

import lombok.Data;

/**
 * Sku库存
 */
@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
