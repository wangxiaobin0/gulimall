package com.mall.product.service.impl;

import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.entity.AttrEntity;
import com.mall.product.service.AttrAttrgroupRelationService;
import com.mall.product.service.AttrService;
import com.mall.product.vo.AttrGroupVo;
import com.mall.product.vo.AttrGroupWithAttrsVo;
import com.mall.product.vo.web.SpuItemAttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.AttrGroupDao;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sun.java2d.windows.GDIRenderer;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationService relationService;

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long categoryId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<AttrGroupEntity>();
        //搜索关键字
        String key = (String) params.get("key");
        //分类id!=0 且搜索关键字
        if (categoryId != 0) {
            queryWrapper.eq("catelog_id", categoryId);
        }
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(obj->{
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void deleteRelation(AttrGroupVo[] groupVo) {
        relationService.deleteRelation(groupVo);
    }

    @Override
    @Transactional
    public void delete(List<Long> asList) {
        //删除group
        this.removeByIds(asList);
        //删除关联表
        relationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", asList));
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupAndAttrByCatelogId(Long catelogId) {
        //根据分类id查询分类下所有的分组
        List<AttrGroupEntity> groupEntityList = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //根据分组查询分组下所有的属性
        List<AttrGroupWithAttrsVo> data = groupEntityList.stream().map(group -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(group, attrGroupWithAttrsVo);
            //根据分组id查询分组中所有的属性
            List<AttrEntity> attrEntityList = attrService.getAttrRelation(group.getAttrGroupId());
            attrGroupWithAttrsVo.setAttrs(attrEntityList);
            return attrGroupWithAttrsVo;
        }).collect(Collectors.toList());
        return data;
    }

    @Override
    public List<SpuItemAttrGroupVo> getGroupVoBySpuId(Long spuId) {
        AttrGroupDao attrGroupDao = this.baseMapper;
        List<SpuItemAttrGroupVo> groupAttrs =  attrGroupDao.getGroupVoBySpuId(spuId);
        return groupAttrs;
    }
}