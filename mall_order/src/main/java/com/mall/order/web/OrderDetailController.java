package com.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderDetailController {

    @GetMapping("/detail.html")
    public String goToDetailPage() {
        return "detail";
    }
}
