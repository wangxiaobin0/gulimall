package com.mall.product.service.impl;

import com.mall.common.constrant.ProductConstant;
import com.mall.product.entity.AttrAttrgroupRelationEntity;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.AttrAttrgroupRelationService;
import com.mall.product.service.AttrGroupService;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.AttrGroupVo;
import com.mall.product.vo.AttrRespVo;
import com.mall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.product.dao.AttrDao;
import com.mall.product.entity.AttrEntity;
import com.mall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrGroupService attrGroupService;

    @Resource
    AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        Integer type = (attrType.equalsIgnoreCase("base") ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        queryWrapper.eq("attr_type", type);
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> respVoList = records.stream().map((attrEntity) -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            //查询分类名
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            attrRespVo.setCatelogName(categoryEntity.getName());

            //查询分组名
            AttrAttrgroupRelationEntity attr_id = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            //属性类型为基本类型且存在分组关联
            if (type == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr_id != null) {
                AttrGroupEntity groupEntity = attrGroupService.getById(attr_id.getAttrGroupId());
                attrRespVo.setGroupName(groupEntity.getAttrGroupName());
            }

            return attrRespVo;
        }).collect(Collectors.toList());
        pageUtils.setList(respVoList);
        return pageUtils;
    }

    @Override
    @Transactional
    public void save(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        //复制AttrVo中的数据到AttrEntity
        BeanUtils.copyProperties(attrVo, attrEntity);
        //保存属性
        this.save(attrEntity);
        //属性类型是基本类型且分组id不为null
        if (attrVo.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attrVo.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            //保存属性与分组关联
            attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
    }

    @Override
    public AttrRespVo getAttrRespVoById(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        //复制attrEntity到attrRespVo
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);
        //查询分类id
        Long[] catelogPath = categoryService.getCatelogPathByCatelogId(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);
        //查询关联表数据,获取分组id
        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
        //不为null,设置分组id
        if (relationEntity != null) {
            attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
        }

        return attrRespVo;
    }

    @Override
    public void updateById(AttrVo attrVo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        //更新属性信息
        this.updateById(attrEntity);
        //更新关联信息
        AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
        relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
        relationEntity.setAttrId(attrVo.getAttrId());

        attrAttrgroupRelationService.saveOrUpdate(relationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", relationEntity.getAttrId()));
    }

    @Override
    public void removeAttr(List<Long> asList) {
        this.removeByIds(asList);
        attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_id", asList));
    }
    @Override
    public List<AttrEntity> getAttrRelation(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> groupId = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));
        if (groupId == null || groupId.size() == 0) {
            return null;
        }
        List<Long> attrIds = groupId.stream().map((group) -> {
            return group.getAttrId();
        }).collect(Collectors.toList());
        List<AttrEntity> entityList = attrDao.selectBatchIds(attrIds);
        return entityList;
    }

    @Override
    public PageUtils getAttrNoRelation(Map<String, Object> params, Long attrgroupId) {
        // 1. 查出group所属分类
        AttrGroupEntity byId = attrGroupService.getById(attrgroupId);
        Long catelogId = byId.getCatelogId();
        // 2. 查出该分类下所有的group
        List<AttrGroupEntity> groupList = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        //所有的group id
        List<Long> groupIds = groupList.stream().map(group -> {
            return group.getAttrGroupId();
        }).collect(Collectors.toList());

        // 3. 查询group已经关联的attr
        List<AttrAttrgroupRelationEntity> collect = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", groupIds));
        List<Long> attrIds = collect.stream().map(r -> {
            return r.getAttrId();
        }).collect(Collectors.toList());

        // 4. 过滤已被关联的attr
        // 4.1. 过滤销售属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        String key = (String) params.get("key");
        // 4.2. 过滤被关联的属性
        if (attrIds != null && attrIds.size() > 0) {
            queryWrapper.and(wrapper -> wrapper.notIn("attr_id", attrIds));
        }
        // 4.3. 添加关键字查询
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    public void addRelation(List<AttrGroupVo> groupVos) {
        attrAttrgroupRelationService.saveBatch(groupVos);
    }

}