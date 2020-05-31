package com.mall.order.vo;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class OrderConfirmVo implements Serializable {
    @Getter @Setter
    private List<MemberAddressVo> address;
    @Getter @Setter
    private List<CartItemVo> items;
    @Getter @Setter
    private BigDecimal integration;
    @Getter @Setter
    private String token;

    private BigDecimal payPrice;

    public BigDecimal getPayPrice() {
        BigDecimal total = new BigDecimal("0");

        if (this.items != null) {
            for (CartItemVo item : this.items) {
                total = total.add(item.getTotalPrice());
            }
        }
        return total;
    }
}

