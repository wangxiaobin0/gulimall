package com.mall.search.web;

import com.mall.search.service.ISearchService;
import com.mall.search.vo.SearchParam;
import com.mall.search.vo.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class SearchController {

    @Autowired
    ISearchService searchService;

    @GetMapping(value = {"/list.html","/search.html"})
    public String search(SearchParam searchParam, Model model) throws IOException {
        SearchResponse searchResponse = searchService.search(searchParam);
        model.addAttribute("searchResponse", searchResponse);
        return "index";
    }
}
