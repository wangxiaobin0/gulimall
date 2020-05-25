package com.mall.auth.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
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
        cookieSerializer.setDomainName("mall.com");
        cookieSerializer.setCookieName("mall_session");
        return cookieSerializer;
    }
}
