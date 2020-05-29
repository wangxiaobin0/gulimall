package com.mall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Data
@Component
@ConfigurationProperties("order.thread")
public class OrderThreadPoolExecutorProperties {

    private Integer corePoolSize = 10;
    private Integer maximumPoolSize = 30;
    private Long keepAliveTime = 5L;
    private TimeUnit unit = TimeUnit.SECONDS;
    private Integer taskQueueCapacity = 50000;
}
