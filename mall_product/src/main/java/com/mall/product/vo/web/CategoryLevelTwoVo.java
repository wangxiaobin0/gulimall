package com.mall.product.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryLevelTwoVo {
    private Long catalog1Id;
    private Long id;
    private String name;

    List<CategoryLevelThree> catalog3List;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryLevelThree {
        private Long catalog2Id;
        private Long id;
        private String name;
    }
}
