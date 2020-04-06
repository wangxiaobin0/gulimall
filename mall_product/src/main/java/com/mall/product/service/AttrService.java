package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.AttrEntity;
import com.mall.product.vo.AttrGroupVo;
import com.mall.product.vo.AttrRespVo;
import com.mall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:32
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId, String attrType);

    void save(AttrVo attrVo);

    AttrRespVo getAttrRespVoById(Long attrId);

    void updateById(AttrVo attrVo);

    void removeAttr(List<Long> asList);

    List<AttrEntity> getAttrRelation(Long attrgroupId);

    PageUtils getAttrNoRelation(Map<String, Object> params, Long attrgroupId);

    void addRelation(List<AttrGroupVo> groupVos);
}

