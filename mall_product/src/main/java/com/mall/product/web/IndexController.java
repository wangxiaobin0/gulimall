package com.mall.product.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mall.product.controller.CategoryController;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.web.CategoryLevelTwoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
@Slf4j
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redisson;

    @Autowired
    RedisTemplate redisTemplate;


    @GetMapping({"/", "index", "index.html"})
    public String goToIndex(Model model) throws JsonProcessingException {
        List<CategoryEntity> list = categoryService.listForIndex();
        model.addAttribute("list", list);
        return "index";
    }

    @GetMapping("/index/json/catalog.json")
    @ResponseBody
    public Map<Long, List<CategoryLevelTwoVo>> getCategoryLevelInfo() throws JsonProcessingException{
        Map<Long, List<CategoryLevelTwoVo>> map = categoryService.getCategoryLevelInfo();
        return map;
    }

}
