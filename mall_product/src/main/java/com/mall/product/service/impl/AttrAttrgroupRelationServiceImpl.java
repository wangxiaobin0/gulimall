package com.mall.product.service.impl;

import com.mall.product.vo.AttrGroupVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.AttrAttrgroupRelationDao;
import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.service.AttrAttrgroupRelationService;

import javax.annotation.Resource;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Resource
    AttrAttrgroupRelationDao relationDao;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void deleteRelation(AttrGroupVo[] groupVo) {
        List<AttrAttrgroupRelationEntity> entityList = Arrays.asList(groupVo).stream().map((group) -> {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            entity.setAttrId(group.getAttrId());
            entity.setAttrGroupId(group.getAttrGroupId());
            return entity;
        }).collect(Collectors.toList());
        relationDao.deleteBatch(entityList);
    }

    @Override
    public void saveBatch(List<AttrGroupVo> groupVos) {
        List<AttrAttrgroupRelationEntity> entityList = groupVos.stream().map(groupVo -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(groupVo, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        this.saveBatch(entityList);
    }

}