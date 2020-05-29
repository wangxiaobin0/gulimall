package com.mall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OrderListController {

    @GetMapping("/list.html")
    public String goToListPage() {
        return "list";
    }
}
