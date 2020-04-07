package com.mall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.mall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author
 * @date 2020/4/6
 */
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    /**
     * attrs
     */
    private List<AttrEntity> attrs;
}
