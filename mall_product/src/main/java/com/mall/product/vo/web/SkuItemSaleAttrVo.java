package com.mall.product.vo.web;

import lombok.Data;

import java.util.List;

@Data
public class SkuItemSaleAttrVo {
    private Long attrId;
    private String attrName;
    private List<SkuAttrValueVo> attrValues;
}
