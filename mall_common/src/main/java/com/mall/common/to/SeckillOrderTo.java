package com.mall.common.to;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class SeckillOrderTo implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * member_id
     */
    private Long memberId;
    /**
     * 订单号
     */
    private String orderSn;

    private Integer num;

    private SeckillSkuRedisTo redisTo;
}
