package com.mall.product.web;

import com.mall.product.controller.CategoryController;
import com.mall.product.entity.CategoryEntity;
import com.mall.product.service.CategoryService;
import com.mall.product.vo.web.CategoryLevelTwoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;
    @GetMapping({"/", "index", "index.html"})
    public String goToIndex(Model model) {
        List<CategoryEntity> list = categoryService.listForIndex();
        model.addAttribute("list", list);
        return "index";
    }

    @GetMapping("/index/json/catalog.json")
    @ResponseBody
    public Map<Long, List<CategoryLevelTwoVo>> getCategoryLevelInfo() {
        Map<Long, List<CategoryLevelTwoVo>> map = categoryService.getCategoryLevelInfo();
        return map;
    }
}
