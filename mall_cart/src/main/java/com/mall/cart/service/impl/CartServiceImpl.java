package com.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.mall.cart.feign.ProductServiceFeign;
import com.mall.cart.intercepter.CartInterceptor;
import com.mall.cart.service.ICartService;
import com.mall.cart.to.UserTo;
import com.mall.cart.vo.Cart;
import com.mall.cart.vo.CartItemVo;
import com.mall.cart.vo.SkuInfoVo;
import com.mall.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements ICartService {

    private static final String CART_PREFIX = "cart:";


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductServiceFeign productServiceFeign;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        CartItemVo vo = new CartItemVo();
        //异步查询sku信息
        CompletableFuture<Void> sku = CompletableFuture.runAsync(() -> {
            R info = productServiceFeign.info(skuId);
            SkuInfoVo skuInfo = info.get("skuInfo", SkuInfoVo.class);
            vo.setSkuId(skuId);
            vo.setTitle(skuInfo.getSkuTitle());
            vo.setImage(skuInfo.getSkuDefaultImg());
            vo.setPrice(skuInfo.getPrice());
            vo.setCount(num);
        }, executor);
        //异步查询sku的销售属性
        CompletableFuture<Void> attrs = CompletableFuture.runAsync(() -> {
            List<String> skuAttr = productServiceFeign.getSkuAttr(skuId);
            vo.setSkuAttr(skuAttr);
        }, executor);
        //都执行完之后再往下执行
        CompletableFuture.allOf(sku, attrs).get();

        UserTo userTo = CartInterceptor.threadLocal.get();
        String key = userTo.getUserId() == null ? CART_PREFIX + userTo.getUserKey() : CART_PREFIX + userTo.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = getHashOps(key);

        //查询是否已经添加到过购物车
        String saved = (String) hashOps.get(skuId.toString());
        //非第一次添加
        if (!StringUtils.isEmpty(saved)) {
            //已经添加过了就更新一下数量
            //用查出来的信息是为了更新sku信息，以防止购物车sku信息与实际不同
            CartItemVo cartItemVo = JSON.parseObject(saved, CartItemVo.class);
            vo.setCount(num + cartItemVo.getCount());
        }
        hashOps.put(skuId.toString(), JSON.toJSONString(vo));
    }

    @Override
    public CartItemVo getCartItemVoBySkuId(Long skuId) {
        UserTo userTo = CartInterceptor.threadLocal.get();
        String key = userTo.getUserId() == null ? CART_PREFIX + userTo.getUserKey() : CART_PREFIX + userTo.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = getHashOps(key);
        String data = (String) hashOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(data, CartItemVo.class);
        return cartItemVo;
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        UserTo userTo = CartInterceptor.threadLocal.get();

        String key = CART_PREFIX + userTo.getUserKey();
        BoundHashOperations<String, Object, Object> hashOps = getHashOps(key);
        List<Object> objects = hashOps.multiGet(hashOps.keys());
        List<CartItemVo> cartItemVos = convertToCartVos(objects);

        //未登录
        if (userTo.getUserId() == null) {
            Cart cart = new Cart();
            cart.setItems(cartItemVos);
            cart.setReduce(new BigDecimal(100));
            return cart;
        } else {
            //已登录,且游客购物车不为空
            if (cartItemVos != null && !cartItemVos.isEmpty()) {
                //合并购物车
                for (CartItemVo cartItemVo : cartItemVos) {
                    this.addToCart(cartItemVo.getSkuId(), cartItemVo.getCount());
                }
                //合并结束，删除游客购物车
                redisTemplate.delete(key);
            }
            //查询登录用户购物车
            String loginUserKey = CART_PREFIX + userTo.getUserId();
            BoundHashOperations<String, Object, Object> loginUser = getHashOps(loginUserKey);
            List<Object> list = loginUser.multiGet(loginUser.keys());
            List<CartItemVo> vos = convertToCartVos(list);
            Cart cart = new Cart();
            cart.setItems(vos);
            cart.setReduce(new BigDecimal(100));
            return cart;
        }
    }

    @Override
    public void changeCheckState(Long skuId, Boolean check) {
        UserTo userTo = CartInterceptor.threadLocal.get();
        String key = userTo.getUserId() == null ? CART_PREFIX + userTo.getUserKey() : CART_PREFIX + userTo.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = getHashOps(key);
        String data = (String) hashOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(data, CartItemVo.class);
        cartItemVo.setCheck(check);
        hashOps.put(skuId.toString(), JSON.toJSONString(cartItemVo));
    }

    @Override
    public void changeSkuCount(Long skuId, Integer count) {
        UserTo userTo = CartInterceptor.threadLocal.get();
        String key = userTo.getUserId() == null ? CART_PREFIX + userTo.getUserKey() : CART_PREFIX + userTo.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = getHashOps(key);
        String data = (String) hashOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(data, CartItemVo.class);
        cartItemVo.setCount(count);
        hashOps.put(skuId.toString(), JSON.toJSONString(cartItemVo));
    }

    @Override
    public void delete(Long skuId) {
        UserTo userTo = CartInterceptor.threadLocal.get();
        String key = userTo.getUserId() == null ? CART_PREFIX + userTo.getUserKey() : CART_PREFIX + userTo.getUserId();
        BoundHashOperations<String, Object, Object> hashOps = getHashOps(key);
        hashOps.delete(skuId.toString());
    }

    private List<CartItemVo> convertToCartVos(List<Object> objects) {
        if (objects != null && !objects.isEmpty()) {
            List<CartItemVo> collect = objects.stream().map(item -> {
                String data = (String) item;
                CartItemVo cartItemVo = JSON.parseObject(data, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
            return collect;
        }
        return null;
    }

    private BoundHashOperations<String, Object, Object> getHashOps(String key) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        return hashOps;
    }
}
