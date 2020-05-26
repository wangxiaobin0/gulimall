package com.mall.cart.controller;

import com.mall.cart.intercepter.CartInterceptor;
import com.mall.cart.to.UserTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    @GetMapping
    public String goToCartPage() {
        UserTo userTo = CartInterceptor.threadLocal.get();
        System.out.println(userTo);
        return "cartList";
    }


    @GetMapping("/success")
    public String goToSuccessPage() {
        return "success";
    }
}
