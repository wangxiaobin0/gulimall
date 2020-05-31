package com.mall.order.web;


import com.mall.order.service.OrderService;
import com.mall.order.vo.OrderPayRespVo;
import com.mall.order.vo.OrderPayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class OrderPayController {

    @Autowired
    OrderService orderService;


    @PostMapping("/pay.html")
    public String goToPayPage(OrderPayVo orderPayVo, Model model) {
        OrderPayRespVo orderPayRespVo = orderService.goToPayPage(orderPayVo);
        model.addAttribute("payInfo", orderPayRespVo);
        return "pay";
    }
}
