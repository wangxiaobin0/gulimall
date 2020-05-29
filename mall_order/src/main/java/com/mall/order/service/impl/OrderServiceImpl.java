package com.mall.order.service.impl;

import com.mall.order.feign.CartServiceFeign;
import com.mall.order.feign.MemberServiceFeign;
import com.mall.order.vo.CartItemVo;
import com.mall.order.vo.MemberAddressVo;
import com.mall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.order.dao.OrderDao;
import com.mall.order.entity.OrderEntity;
import com.mall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberServiceFeign memberServiceFeign;

    @Autowired
    CartServiceFeign cartServiceFeign;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo getCartInfo() throws ExecutionException, InterruptedException {
        OrderConfirmVo vo = new OrderConfirmVo();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            //解决异步请求丢失请求头
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //1. 查询收货地址列表
            List<MemberAddressVo> address = memberServiceFeign.getAddressByMemberId();
            vo.setAddress(address);
        }, executor);

        CompletableFuture<Void> itemFuture = CompletableFuture.runAsync(() -> {
            //解决异步请求丢失请求头
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2. 查询结算商品
            List<CartItemVo> checkedItems = cartServiceFeign.getCheckedItems();
            vo.setItems(checkedItems);
        }, executor);

        CompletableFuture.allOf(addressFuture, itemFuture).get();
        //3. 查询优惠信息
        vo.setIntegration(new BigDecimal("100"));
        return vo;
    }

}