package com.mall.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author
 * @date 2020/4/7
 */
@Data
public class SkuReductionTo {
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * 满多少
     */
    private BigDecimal fullPrice;
    /**
     * 减多少
     */
    private BigDecimal reducePrice;

    private Integer priceStatus;

    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;

    private Integer countStatus;

    private List<MemberPrice> memberPrice;
}
