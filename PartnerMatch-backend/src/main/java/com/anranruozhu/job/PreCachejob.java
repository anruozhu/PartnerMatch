package com.anranruozhu.job;

import com.anranruozhu.mapper.UserMapper;
import com.anranruozhu.model.domain.User;
import com.anranruozhu.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.shadow.org.terracotta.statistics.Time;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author anranruozhu
 * @ClassName PreCachejob
 * @description 缓存预热
 * @create 2024/6/16 下午9:50
 **/
@Component
@Slf4j
public class PreCachejob {


    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    //作为测试手动指定一个热点用户，实际上要通过用户的数据访问情况来进行对热点用户的标记。
    private List<Long> mianUserId= Arrays.asList(1L);
    @Resource
    private RedissonClient redissonClient;

    /**
     *对热点用户的数据进行缓存预热，提高用户访问体验
     * 适用于后端数据修改并不频繁，并且数据量较大的数据。
     * 优点：
     * 1.提高响应的效率
     * 2.提高系统的吞吐量：减少客户端访问后端数据库或数据源的请求次数
     * 3.降低后端负载
     */
    @Scheduled(cron = "0 57 15 * * *")
    synchronized void doCacheRecommendUser(){

        RLock lock = redissonClient.getLock("anranruozhu:preCachejob:doCacheRecommendUser:lock");
        try {
            if(lock.tryLock(0,300000,TimeUnit .MILLISECONDS)){
                for(Long userId:mianUserId){
                    System.out.println("获取到了lock进行缓存预热");
                   // Thread.sleep(150000);
                    String redisKey = String.format("xyz:user:recommend:%s", userId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User>   userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    // 写缓存
                    try {
                        valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                        log.info(redisKey);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }

                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
        }finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }

}
