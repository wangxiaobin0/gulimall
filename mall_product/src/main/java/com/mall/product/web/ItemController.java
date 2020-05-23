package com.mall.product.web;

import com.mall.product.service.SkuInfoService;
import com.mall.product.vo.web.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String item(@PathVariable("skuId") Long skuId, Model model) {

        SkuItemVo vo = skuInfoService.getItemInfo(skuId);
        model.addAttribute("item", vo);
        return "item";
    }
}
