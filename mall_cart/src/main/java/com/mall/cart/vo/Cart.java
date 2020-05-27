package com.mall.cart.vo;


import java.math.BigDecimal;
import java.util.List;

public class Cart {
    private List<CartItemVo> items;
    private Integer countNum;
    private BigDecimal totalAmount;
    private BigDecimal reduce = new BigDecimal(0);

    public List<CartItemVo> getItems() {
        return items;
    }

    public void setItems(List<CartItemVo> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (this.items != null && !items.isEmpty()) {
            for (CartItemVo item : this.items) {
                count += item.getCount();
            }
        }
        return countNum;
    }
    public BigDecimal getTotalAmount() {
        BigDecimal total = new BigDecimal(0);
        if (this.items == null || this.items.isEmpty()) {
            return total;
        }
        for (CartItemVo item : this.items) {
            total = total.add(item.getTotalPrice());
        }
        return total;
    }
    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
