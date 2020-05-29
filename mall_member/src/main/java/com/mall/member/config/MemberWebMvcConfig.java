package com.mall.member.config;

import com.mall.member.interceptor.MemberRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MemberWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    MemberRequestInterceptor memberRequestInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(memberRequestInterceptor).addPathPatterns("/member/memberreceiveaddress/**");
    }
}
