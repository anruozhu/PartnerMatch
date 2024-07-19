package com.anranruozhu.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author anranruozhu
 * @ClassName RedisTest
 * @description redis测试类
 * @create 2024/6/15 下午8:42
 **/
@Slf4j
@SpringBootTest
public class RedisTest {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void test(){
        log.error(String.valueOf(redisTemplate.getClass()));
//        ValueOperations<String, String> stringOperations = stringRedisTemplate.opsForValue();
        ValueOperations valueOperations = redisTemplate.opsForValue();
        log.error(String.valueOf(valueOperations.getClass()));
        valueOperations.set("xyzString","gogo");
        valueOperations.set("xyzInteger",1);
        valueOperations.set("xyzDouble",2.0);
        Assertions.assertEquals(valueOperations.get("xyzString"), "gogo");
        Assertions.assertTrue((Integer)(valueOperations.get("xyzInteger"))==1);
        Assertions.assertTrue((Double)(valueOperations.get("xyzDouble"))==2.0);
        System.out.println(valueOperations.get("xyzString"));
       log.info(String.valueOf(valueOperations.setIfAbsent("xyzString1","opop")));
        RedisOperations operations = valueOperations.getOperations();
        valueOperations.getAndExpire("xyzString1", 6, TimeUnit.SECONDS);
    }
}
