package com.mall.ware.dao;

import com.mall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:44:05
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    Long getSkuStock(@Param("id") Long id);

    Integer lockStock(@Param("wareId") Long wareId, @Param("skuId") Long skuId, @Param("num") Integer num);

    List<Long> getWareIdBySku(@Param("skuId") Long skuId, @Param("num") Integer num);
}
