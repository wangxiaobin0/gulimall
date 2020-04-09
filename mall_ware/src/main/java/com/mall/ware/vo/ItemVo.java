package com.mall.ware.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author
 * @date 2020/4/7
 */
@Data
public class ItemVo implements Serializable {
    private Long itemId;
    private Integer status;
    private String reason;
}
