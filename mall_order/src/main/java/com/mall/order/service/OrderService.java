package com.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mall.common.utils.PageUtils;
import com.mall.order.entity.OrderEntity;
import com.mall.order.vo.OrderConfirmVo;
import com.mall.order.vo.OrderPayRespVo;
import com.mall.order.vo.OrderPayVo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author wangxb
 * @email 1378975974@qq.com
 * @date 2020-04-04 15:40:24
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo getCartInfo() throws ExecutionException, InterruptedException;

    OrderPayRespVo goToPayPage(OrderPayVo orderPayVo);
}

