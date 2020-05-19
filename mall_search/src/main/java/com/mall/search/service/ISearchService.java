package com.mall.search.service;

import com.mall.search.vo.SearchParam;
import com.mall.search.vo.SearchResponse;

import java.io.IOException;

public interface ISearchService {
    SearchResponse search(SearchParam searchParam) throws IOException;
}
