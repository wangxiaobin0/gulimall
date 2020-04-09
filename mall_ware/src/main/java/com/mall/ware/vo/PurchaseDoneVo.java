package com.mall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author
 * @date 2020/4/7
 */
@Data
public class PurchaseDoneVo implements Serializable {
    @NotNull
    private Long id;
    private List<ItemVo> items;
}
