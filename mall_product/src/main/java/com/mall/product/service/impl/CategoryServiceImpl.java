package com.mall.product.service.impl;

import com.mall.product.dao.CategoryBrandRelationDao;
import com.mall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.LongHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.CategoryDao;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryService categoryService;

    @Resource
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> entityList = categoryService.list();
        List<CategoryEntity> levelMenu = entityList.stream().filter(entity -> {
            //过滤条件. 获取1级菜单,level==1
            return entity.getCatLevel() == 1;
        }).map((entity) -> {
            //组装数据,
            entity.setChildren(getChildren(entity, entityList));
            return entity;
        }).sorted((menu1, menu2) -> {
            //排序
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());

        return levelMenu;
    }

    public List<CategoryEntity> getChildren(CategoryEntity father, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter((entity) -> {
            return entity.getParentCid() == father.getCatId();
        }).map((entity) -> {
            entity.setChildren(getChildren(entity, all));
            return entity;
        }).sorted((menu1, menu2) -> {
            return menu1.getSort() - menu2.getSort();
        }).collect(Collectors.toList());

        return children;
    }

    @Override
    public Long[] getCatelogPathByCatelogId(Long catelogId) {
        List<Long> path = new ArrayList<>();
        //根据当前分类id,查出完整的分类id(包括父级和当前)
        path = getFullPath(catelogId, path);
        return path.toArray(new Long[path.size()]);
    }

    @Override
    @Transactional
    public void updateDetail(CategoryEntity category) {
        //更新分类信息
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            //更新关联表中分类信息
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }

    }

    /**
     * 递归获取所有父级分类的id
     * @param catelogId
     * @param path
     * @return
     */
    List<Long> getFullPath(Long catelogId, List<Long> path) {
        //当前分类
        CategoryEntity byId = categoryService.getById(catelogId);
        //分类等级不等于1说明还有父级
        if (byId.getCatLevel() != 1) {
            //获取父级分类id
            path = getFullPath(byId.getParentCid(), path);
        }
        //记录当前分类id.递归查询最后一次找到的就是一级分类,所以不需要在index==0处插入
        path.add(byId.getCatId());
        return  path;
    }
}