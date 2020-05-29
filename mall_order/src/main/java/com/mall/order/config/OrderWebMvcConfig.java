package com.mall.order.config;

import com.mall.order.interceptor.OrderRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OrderWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    OrderRequestInterceptor orderRequestInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderRequestInterceptor).addPathPatterns("/**");
    }
}
