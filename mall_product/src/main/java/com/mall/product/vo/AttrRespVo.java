package com.mall.product.vo;

import lombok.Data;

/**
 * @author
 * @date 2020/4/6
 */
@Data
public class AttrRespVo extends AttrVo {
    /**
     * 分类名称
     */
    private String catelogName;
    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 查询分类id
     */
    private Long[] catelogPath;
}
