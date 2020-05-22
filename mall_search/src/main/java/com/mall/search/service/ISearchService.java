package com.mall.search.service;

import com.mall.search.vo.SearchParam;
import com.mall.search.vo.SearchResult;

import java.io.IOException;

public interface ISearchService {
    SearchResult search(SearchParam searchParam) throws IOException;
}
