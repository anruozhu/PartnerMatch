package com.anranruozhu.service;


import com.anranruozhu.model.domain.User;
import com.google.gson.Gson;
import org.junit.Assert;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.*;

@SpringBootTest
public class UserServiceTest {

    @Resource()
    private UserService userService;

    //自定义连接池
    private final ExecutorService executorService=new ThreadPoolExecutor(20,1000,10000, TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>(10000));

    @Test
    void testSearchUserByTags() {
        List<String> tagNameList= Arrays.asList("java","python");
        List<User> userList = userService.searchUserByTags(tagNameList);
        Assert.assertNotNull(userList);
    }
    /**
     * 批量插入数据
     */
    @Test
    void insertUserData(){
        Random random=new Random();
        //计时器
        StopWatch sw=new StopWatch();
        sw.start();
        int NUM_INSERT=10000;
        List<User> userList=new ArrayList<>();
        for(int i=0;i<NUM_INSERT;i++){
            User user=new User();
            user.setUsername("假熊猫");
            user.setUserAccount("test");
            user.setAvatarUrl("https://images.zsxq.com/FnJjtk0PJTi_4Jhp06An52fqo39L?e=1722441599&token=kIxbL07-8jAj8w1n4s9zv64FuZZNEATmlU_Vm6zD:fqKfjvf07L6wiGVZcAwauTWbQTk=");
            user.setGender(i%2);
            user.setUserPassword("9a9d5dc96617ad89f51f832c805285f2");
            user.setPhone("19188886504");
            user.setEmail(random.nextInt(99999999 - 10000000 + 1) + 10000000+"@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode((random.nextInt(999999 - 100000 + 1) + 10000000)+"");
            Set<String> tags = new HashSet<>(Arrays.asList("java", "C++", "python", "Go", "Rust", "Lisp", "emo", "大一", "大二", "大三", "考研", "求职"));
            final float EXPECTED = 4f/tags.size();
            for(Iterator<String> itr = tags.iterator(); itr.hasNext();){
                itr.next();
                if(Math.random()>=EXPECTED){
                    itr.remove();
                }
            }
            Gson gson = new Gson();
            user.setTags(gson.toJson(tags));
            //userMapper.insert(user);
            userList.add(user);
        }
        userService.saveBatch(userList);
        sw.stop();
        System.out.println(sw.getTotalTimeMillis());
        //402880
    }
    /**
     * 并发批量插入数据
     */
    @Test
    void insertConcurrencyUserData(){
        //计时器
        StopWatch sw=new StopWatch();
        //生成随机数据
        Random random=new Random();
        sw.start();
        //划分为十组来进行插入
        int batchSize=5000;
        int j=0;
        List<CompletableFuture<Void>> futureList=new ArrayList<>();
        for(int i=0;i<20; i++){
            List<User> userList=new ArrayList<>();
            while(true){{
                j++;
                User user=new User();
                user.setUsername("假熊猫");
                user.setUserAccount("test");
                user.setAvatarUrl("https://images.zsxq.com/FnJjtk0PJTi_4Jhp06An52fqo39L?e=1722441599&token=kIxbL07-8jAj8w1n4s9zv64FuZZNEATmlU_Vm6zD:fqKfjvf07L6wiGVZcAwauTWbQTk=");
                user.setGender(j%2);
                user.setUserPassword("9a9d5dc96617ad89f51f832c805285f2");
                user.setPhone("19188886504");
                user.setEmail(random.nextInt(99999999 - 10000000 + 1) + 10000000+"@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode((random.nextInt(999999 - 100000 + 1) + 10000000)+"");
                Set<String> tags = new HashSet<>(Arrays.asList("java", "C++", "python", "Go", "Rust", "Lisp", "emo", "大一", "大二", "大三", "考研", "求职"));
                final float EXPECTED = 4f/tags.size();
                for(Iterator<String> itr = tags.iterator(); itr.hasNext();){
                    itr.next();
                    if(Math.random()>=EXPECTED){
                        itr.remove();
                    }
                }
                Gson gson = new Gson();
                user.setTags(gson.toJson(tags));
                userList.add(user);
                if(j%batchSize==0) break;
            }}
            //处理异步请求
            CompletableFuture<Void> future=CompletableFuture.runAsync(()->{
                System.out.println("TreadName:"+Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            },  executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        sw.stop();
        System.out.println(sw.getTotalTimeMillis());
    }

}