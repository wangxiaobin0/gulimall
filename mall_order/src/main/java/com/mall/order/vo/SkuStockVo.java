package com.mall.order.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuStockVo implements Serializable {
    private Long skuId;
    private Integer num;
}
