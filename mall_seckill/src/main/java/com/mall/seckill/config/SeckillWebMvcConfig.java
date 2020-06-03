package com.mall.seckill.config;

import com.mall.seckill.interceptor.SeckillRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SeckillWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    SeckillRequestInterceptor seckillRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seckillRequestInterceptor).addPathPatterns("/seckill");
    }
}
