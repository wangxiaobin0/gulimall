package com.mall.search.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParam {

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 三级分类Id
     */
    private Long catelog3Id;


    /**
     * 排序字段
     *  sort = saleCount_asc/desc
     *  sort = skuPrice_asc/desc
     *  sort = hotScore_asc/desc
     */
    private String sort;

    /**
     * 过滤条件：是否只显示有货
     * hasStock = 0/1
     */
    private Integer hasStock;

    /**
     * 过滤条件：价格区间
     * skuPrice = 1_500/_500/500_
     */
    private String skuPrice;

    /**
     * 过滤条件：品牌id，可多选
     */
    List<Long> brandId;

    /**
     * 过滤条件：属性
     * attrs=1_5寸:6寸
     */
    List<String> attrs;

    /**
     * 页码
     */
    Integer pageNum = 1;

}
