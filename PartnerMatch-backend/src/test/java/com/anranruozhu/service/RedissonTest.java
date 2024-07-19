package com.anranruozhu.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.lang.invoke.SerializedLambda;
import java.util.concurrent.TimeUnit;

/**
 * @author anranruozhu
 * @ClassName RedissonTest
 * @description
 * @create 2024/6/22 上午11:05
 **/
@Slf4j
@SpringBootTest
public class RedissonTest {
    @Resource
    RedissonClient redissonClient;
    @Test
    public void testRedisson() {
        //Redission可像在java本地操控集合一样来对数据集进行操作。
        RList<String> list = redissonClient.getList("xyz-list");
        list.add("xc");
        list.add("xc");
        list.add("xc");
        System.out.println(list.get(0));
//       list.remove(0);
    }
    @Test
    void testWatchDig(){
        //Redission内部具有的看门狗机制，设置过期时间为-1，那么redis会创建一个过期时间为30s的对象，并且会在系统线程内部起一个进程对占有锁的线程
        //进行监听，当具有锁的线程还存活着时就会自动去对锁进行续期。
        //当线程挂掉后该锁就会在30s后自动销毁。
        RLock lock = redissonClient.getLock("anranruozhu:preCachejob:doCacheRecommendUser:lock");
        try {
            //只有一个线程可以获取到锁
            if(lock.tryLock(0,-1, TimeUnit.MILLISECONDS))
            {
                Thread.sleep(300000);
                System.out.println("getlock: "+Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }finally {
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
