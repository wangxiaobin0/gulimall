package com.mall.product.vo.web;

import lombok.Data;

import java.util.List;

@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<SpuItemAttrVo> attrs;
}
