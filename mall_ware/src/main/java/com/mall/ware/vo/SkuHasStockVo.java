package com.mall.ware.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * Sku库存
 */
@Data
public class SkuHasStockVo implements Serializable {
    private Long skuId;
    private Boolean hasStock;
}
