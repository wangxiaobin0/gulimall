package com.mall.product.dao;

import com.mall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.product.vo.web.SpuItemAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:42:32
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVo> getGroupVoBySpuId(@Param("spuId") Long spuId);
}
