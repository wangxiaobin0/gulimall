package com.mall.order.web;

import com.mall.order.service.OrderService;
import com.mall.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class OrderConfirmController {

    @Autowired
    OrderService orderService;

    @GetMapping
    public String getCartInfo(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo vo =  orderService.getCartInfo();
        model.addAttribute("vo", vo);
        return "confirm";
    }
}
