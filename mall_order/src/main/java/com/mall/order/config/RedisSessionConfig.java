package com.mall.order.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.mall.common.constrant.RedisSessionConstant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class RedisSessionConfig {

    @Bean
    @ConditionalOnClass(RedisSerializer.class)
    public RedisSerializer redisSerializer() {
        return new FastJsonRedisSerializer(Object.class);
    }

    @Bean
    @ConditionalOnClass(CookieSerializer.class)
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName(RedisSessionConstant.DOMAIN_NAME);
        cookieSerializer.setCookieName(RedisSessionConstant.COOKIE_NAME);
        cookieSerializer.setUseHttpOnlyCookie(true);
        return cookieSerializer;
    }
}
