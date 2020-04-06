package com.mall.common.constrant;

import lombok.Getter;

/**
 * @author
 * @date 2020/4/6
 */
public class ProductConstant {

    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");
        /**
         * 编号
         */
        @Getter
        private Integer code;
        /**
         * 描述
         */
        private String msg;

        AttrEnum(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

}
