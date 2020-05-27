package com.mall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class CartThreadExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(CartThreadPoolExecutorProperties properties) {
        return new ThreadPoolExecutor(
                properties.getCorePoolSize(),
                properties.getMaximumPoolSize(),
                properties.getKeepAliveTime(),
                properties.getUnit(),
                new LinkedBlockingDeque<>(properties.getTaskQueueCapacity()),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }
}
