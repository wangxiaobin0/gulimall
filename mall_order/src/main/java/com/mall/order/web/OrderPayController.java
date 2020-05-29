package com.mall.order.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderPayController {

    @GetMapping("/pay.html")
    public String goToPayPage() {
        return "pay";
    }
}
