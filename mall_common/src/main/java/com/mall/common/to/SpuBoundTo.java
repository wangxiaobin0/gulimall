package com.mall.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author
 * @date 2020/4/7
 */
@Data
public class SpuBoundTo {

    /**
     *
     */
    private Long spuId;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;

}
