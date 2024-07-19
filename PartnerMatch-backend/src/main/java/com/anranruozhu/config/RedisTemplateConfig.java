package com.anranruozhu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author anranruozhu
 * @ClassName RedisConfig
 * @description reids的自定义配置类
 * @create 2024/6/15 下午8:55
 **/
@Configuration
public class RedisTemplateConfig {
    @Bean
    public RedisTemplate<Object,Object> redisTemplate(RedisConnectionFactory conectionfantory){
        RedisTemplate<Object,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.java());
        redisTemplate.setConnectionFactory(conectionfantory);
        return redisTemplate;
    }
}
@Configuration
class RedisTemplateConfig2 {
    @Bean("FUCK")
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory conectionfantory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setConnectionFactory(conectionfantory);
        return redisTemplate;
    }
}