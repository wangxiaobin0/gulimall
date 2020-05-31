package com.mall.order.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderPayVo implements Serializable {

    /**
     * 收货地址id
     */
    private Long addrId;

    private BigDecimal payPrice;

    private Integer payType = 1;

    private String token;
}
