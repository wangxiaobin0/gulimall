package com.mall.search.vo;

import com.mall.common.to.SkuEsModel;
import lombok.Data;

import java.util.List;
import java.util.ListResourceBundle;

@Data
public class SearchResult {

    /**
     * 查询数据
     */
    private List<SkuEsModel> products;

    /**
     * 分页信息：pageNum页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPage;

    private List<BrandVo> brands;

    private List<CategoryVo> categories;

    private List<AttrVo> attrs;

    /**
     * 品牌信息
     */
    @Data
    public static class BrandVo {
        /**
         * 品牌id
         */
        private Long brandId;

        /**
         * 品牌名称
         */
        private String brandName;

        /**
         * 品牌图片
         */
        private String brandImg;
    }

    @Data
    public static class CategoryVo {
        private Long cateId;
        private String cateName;
    }

    /**
     * 过滤属性
     */
    @Data
    public static class AttrVo {
        /**
         * 属性id
         */
        private Long attrId;
        /**
         * 属性名称
         */
        private String attrName;
        /**
         * 属性值
         */
        private List<String> attrValue;
    }
}
