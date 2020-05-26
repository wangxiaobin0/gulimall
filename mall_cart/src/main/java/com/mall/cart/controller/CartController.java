package com.mall.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CartController {

    @GetMapping
    public String goToCartPage() {
        return "cartList";
    }


    @GetMapping("/success")
    public String goToSuccessPage() {
        return "success";
    }
}
