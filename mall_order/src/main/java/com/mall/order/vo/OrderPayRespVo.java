package com.mall.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderPayRespVo implements Serializable {
    private String orderSn;
    private BigDecimal payPrice;
}
