package com.mall.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.mall.common.constrant.OrderConstant;
import com.mall.common.constrant.OrderMQConstant;
import com.mall.common.constrant.WareMQConstant;
import com.mall.common.to.OrderTo;
import com.mall.common.to.SeckillOrderTo;
import com.mall.common.to.SeckillSkuRedisTo;
import com.mall.common.utils.R;
import com.mall.common.vo.MemberEntity;
import com.mall.order.entity.OrderItemEntity;
import com.mall.order.enume.OrderStatusEnum;
import com.mall.order.feign.CartServiceFeign;
import com.mall.order.feign.MemberServiceFeign;
import com.mall.order.feign.ProductServiceFeign;
import com.mall.order.feign.WareServiceFeign;
import com.mall.order.interceptor.OrderRequestInterceptor;
import com.mall.order.service.OrderItemService;
import com.mall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.CollectionUtils;
import org.springframework.cloud.commons.util.IdUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.common.utils.PageUtils;
import com.mall.common.utils.Query;

import com.mall.order.dao.OrderDao;
import com.mall.order.entity.OrderEntity;
import com.mall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private static final String CART_PREFIX = "cart:";
    @Autowired
    MemberServiceFeign memberServiceFeign;

    @Autowired
    CartServiceFeign cartServiceFeign;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    ProductServiceFeign productServiceFeign;

    @Autowired
    WareServiceFeign wareServiceFeign;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    RabbitTemplate rabbitTemplate;

    private final ThreadLocal<OrderPayVo> threadLocal = new ThreadLocal<>();

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
        //设置token，校验幂等性
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        vo.setToken(token);

        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN + getLoginUser().getId(), token);
        return vo;
    }

    @Override
    @Transactional
    public OrderPayRespVo goToPayPage(OrderPayVo orderPayVo) {
        //1. 校验token
        String key = OrderConstant.ORDER_TOKEN + getLoginUser().getId();
        String redisToken = redisTemplate.opsForValue().get(key);
        if (!orderPayVo.getToken().equals(redisToken)) {
            throw new RuntimeException("请勿重复提交");
        }
        //校验通过，删除key
        redisTemplate.delete(key);
        //2. 创建订单
        threadLocal.set(orderPayVo);
        OrderEntity orderEntity = initialOrder();
        //2.1.  查询收货地址
        addAddress(orderEntity);
        //2.2.  查询购物车中已勾选订单
        List<OrderItemEntity> orderItemList = addOrderItem(orderEntity.getOrderSn());
        //3. 对价
        //4. 下订单
        this.save(orderEntity);
        orderItemService.saveBatch(orderItemList);
        //5. 锁库存
        List<SkuStockVo> skuStockVos = orderItemList.stream().map(item -> {
            SkuStockVo vo = new SkuStockVo();
            vo.setOrderSn(orderEntity.getOrderSn());
            vo.setSkuId(item.getSkuId());
            vo.setNum(item.getSkuQuantity());
            return vo;
        }).collect(Collectors.toList());
        R r = wareServiceFeign.lockStock(skuStockVos);
        if (!r.get("code").equals(0)) {
            throw new RuntimeException("库存扣减失败");
        }
        //int i = 10 / 0;
        //下单成功删除购物车
        //redisTemplate.delete(CART_PREFIX + getLoginUser().getId());

        //下单成功，发送消息，防止出现超时取消订单，但库存还被锁定的情况
        rabbitTemplate.convertAndSend(OrderMQConstant.ORDER_EXCHANGE, OrderMQConstant.ORDER_CREATE_ROUTING_KEY, orderEntity);

        OrderPayRespVo orderPayRespVo = new OrderPayRespVo();
        orderPayRespVo.setOrderSn(orderEntity.getOrderSn());
        orderPayRespVo.setPayPrice(orderEntity.getPayAmount());
        return orderPayRespVo;
    }

    @Override
    public OrderEntity getByOrderSn(String orderSn) {
        OrderEntity orderEntity = this.getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    @Transactional
    public void cancelOrder(OrderEntity orderEntity) {
        log.info("订单{}超时，取消订单", orderEntity.getOrderSn());
        //更新订单状态
        OrderEntity update = new OrderEntity();
        update.setId(orderEntity.getId());
        update.setStatus(OrderStatusEnum.CANCLED.getCode());
        this.updateById(update);
        log.info("订单{}超时，发送消息补偿库存", orderEntity.getOrderSn());
        //发送消息，主动补偿库存
        OrderTo orderTo = new OrderTo();
        BeanUtils.copyProperties(orderEntity, orderTo);
        rabbitTemplate.convertAndSend(WareMQConstant.WARE_EXCHANGE, WareMQConstant.WARE_UNLOCK_ROUTING_KEY, orderTo);
    }

    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {
        //保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setMemberId(orderTo.getMemberId());
        orderEntity.setOrderSn(orderTo.getOrderSn());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        this.save(orderEntity);

        //保存秒杀商品信息
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        SeckillSkuRedisTo redisTo = orderTo.getRedisTo();
        orderItemEntity.setOrderSn(orderTo.getOrderSn());
        orderItemEntity.setRealAmount(redisTo.getSeckillCount().multiply(new BigDecimal(orderTo.getNum().toString())));
        orderItemEntity.setSkuId(redisTo.getSkuId());
        orderItemEntity.setSkuQuantity(orderTo.getNum());
        orderItemService.save(orderItemEntity);

        //TODO:发送延时消息，在订单自动关闭时间后核对订单状态,判断是否需要补偿库存
    }

    private List<OrderItemEntity> addOrderItem(String orderSn) {
        List<CartItemVo> checkedItems = cartServiceFeign.getCheckedItems();
        List<OrderItemEntity> list = checkedItems.stream().map(item -> {
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            orderItemEntity.setOrderSn(orderSn);
            orderItemEntity.setSkuQuantity(item.getCount());
            //spu信息
            R r = productServiceFeign.infoBySkuId(item.getSkuId());
            SpuInfoVo spuInfo = r.get("spuInfo", SpuInfoVo.class);
            if (spuInfo == null) {
                throw new RuntimeException("spuInfo获取失败");
            }
            orderItemEntity.setSpuId(spuInfo.getId());
            orderItemEntity.setSpuName(spuInfo.getSpuName());
            orderItemEntity.setCategoryId(spuInfo.getCatalogId());
            //sku信息
            orderItemEntity.setSkuId(item.getSkuId());
            orderItemEntity.setSkuName(item.getTitle());
            orderItemEntity.setSkuPic(item.getImage());
            orderItemEntity.setSkuPrice(item.getTotalPrice());
            orderItemEntity.setSkuAttrsVals(item.getSkuAttr().toString());
            //优惠
            orderItemEntity.setRealAmount(item.getTotalPrice());
            //积分
            orderItemEntity.setGiftIntegration(item.getTotalPrice().intValue());
            orderItemEntity.setGiftGrowth(item.getTotalPrice().intValue());
            return orderItemEntity;
        }).collect(Collectors.toList());
        return list;
    }

    /**
     * 添加地址信息
     * @param orderEntity
     * @return
     */
    private void addAddress(OrderEntity orderEntity) {
        R addrInfo = memberServiceFeign.info(threadLocal.get().getAddrId());
        MemberAddressVo address = addrInfo.get("memberReceiveAddress", MemberAddressVo.class);
        if (address == null) {
            throw new RuntimeException("获取收货地址异常");
        }
        //省
        orderEntity.setReceiverProvince(address.getProvince());
        //市
        orderEntity.setReceiverCity(address.getCity());
        //区
        orderEntity.setReceiverRegion(address.getRegion());
        //详细地址
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        //邮编
        orderEntity.setReceiverPostCode(address.getPostCode());
        //收货人名
        orderEntity.setReceiverName(address.getName());
        //收货人手机号
        orderEntity.setReceiverPhone(address.getPhone());
    }

    /**
     * 初始化订单
     * @return
     */
    private OrderEntity initialOrder() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setPayAmount(threadLocal.get().getPayPrice());
        orderEntity.setMemberId(getLoginUser().getId());
        orderEntity.setMemberUsername(getLoginUser().getUsername());
        orderEntity.setOrderSn(IdWorker.getTimeId());
        orderEntity.setCreateTime(new Date());
        orderEntity.setPayType(threadLocal.get().getPayType());
        orderEntity.setSourceType(1);
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setIntegration(threadLocal.get().getPayPrice().intValue());
        orderEntity.setGrowth(threadLocal.get().getPayPrice().intValue());
        return orderEntity;
    }

    private MemberEntity getLoginUser(){
        return OrderRequestInterceptor.getLoginUser();
    }
}