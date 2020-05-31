package com.mall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuWareVo implements Serializable {
    private Long skuId;
    private Integer num;
    private List<Long> wareIds;
}
