package com.mall.cart.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.mall.common.constrant.RedisSessionConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class CartRedisSessionConfig {

    /**
     * RedisSession序列化器
     * @return
     */
    @Bean
    RedisSerializer redisSerializer() {
        return new FastJsonRedisSerializer(Object.class);
    }

    @Bean
    CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        //设置cookie作用域
        cookieSerializer.setDomainName(RedisSessionConstant.DOMAIN_NAME);
        cookieSerializer.setCookieName(RedisSessionConstant.COOKIE_NAME);
        cookieSerializer.setUseHttpOnlyCookie(true);

        return cookieSerializer;
    }
}
