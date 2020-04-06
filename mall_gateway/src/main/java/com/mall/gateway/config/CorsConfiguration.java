package com.mall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author
 * @date 2020/4/4
 */
@Configuration
public class CorsConfiguration {
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource config = new UrlBasedCorsConfigurationSource();
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        //允许的请求头
        configuration.addAllowedHeader("*");
        //允许的请求方式
        configuration.addAllowedMethod("*");
        //允许的Origin
        configuration.addAllowedOrigin("*");
        //允许携带cookie
        configuration.setAllowCredentials(true);
        config.registerCorsConfiguration("/**", configuration);
        CorsWebFilter corsWebFilter = new CorsWebFilter(config);
        return corsWebFilter;
    }
}
