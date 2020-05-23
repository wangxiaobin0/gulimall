package com.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.product.entity.AttrGroupEntity;
import com.mall.product.vo.AttrGroupVo;
import com.mall.product.vo.AttrGroupWithAttrsVo;
import com.mall.product.vo.web.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:32
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long categoryId);

    void deleteRelation(AttrGroupVo[] groupVo);

    void delete(List<Long> asList);

    List<AttrGroupWithAttrsVo> getAttrGroupAndAttrByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getGroupVoBySpuId(Long spuId);
}

