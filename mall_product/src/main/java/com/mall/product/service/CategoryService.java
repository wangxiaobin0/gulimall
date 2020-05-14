package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.vo.web.CategoryLevelTwoVo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:32
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    Long[] getCatelogPathByCatelogId(Long catelogId);

    void updateDetail(CategoryEntity category);

    List<CategoryEntity> listForIndex();

    Map<Long, List<CategoryLevelTwoVo>> getCategoryLevelInfo();
}

