package com.anranruozhu.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;


/**
 * @author anranruozhu
 * @ClassName RedissionConfig
 * @description Redission配置类
 * @create 2024/6/22 上午10:43
 **/
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String port;
    private String host;
    private String password;

    @Bean
    public RedissonClient redissonClient() throws IOException {
        //1.创建配置
        Config config = new Config();
        String address=String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(address).setDatabase(2).setPassword(password);
        //2.创建实例
        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }

}
