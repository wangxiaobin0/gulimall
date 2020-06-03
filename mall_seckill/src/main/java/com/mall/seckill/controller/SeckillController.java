package com.mall.seckill.controller;

import com.mall.seckill.service.SeckillService;
import com.mall.seckill.to.SeckillSkuRedisTo;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping
    @ResponseBody
    public List<SeckillSkuRedisTo> todaySeckill() {
        List<SeckillSkuRedisTo> to = seckillService.todaySeckill();
        return to;
    }

    @GetMapping("/{skuId}")
    @ResponseBody
    public SeckillSkuRedisTo secInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo seckillSkuRedisTo = seckillService.secInfo(skuId);
        return seckillSkuRedisTo;
    }

    @GetMapping("/seckill")
    public String seckill(@RequestParam("skuId") Long skuId,
                          @RequestParam("sessionId") Long sessionId,
                          @RequestParam("token") String token,
                          @RequestParam("num") Integer num,
                          Model model) {
        String orderSn = seckillService.createSeckillOrder(skuId, sessionId, token, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
