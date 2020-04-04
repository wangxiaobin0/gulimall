package com.mall.coupon.dao;

import com.mall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:35:51
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
