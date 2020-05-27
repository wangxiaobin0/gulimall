package com.mall.cart.service;

import com.mall.cart.vo.Cart;
import com.mall.cart.vo.CartItemVo;

import java.util.concurrent.ExecutionException;

public interface ICartService {

    void addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItemVo getCartItemVoBySkuId(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void changeCheckState(Long skuId, Boolean check);

    void changeSkuCount(Long skuId, Integer count);

    void delete(Long skuId);
}
