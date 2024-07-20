package com.anranruozhu.service.impl;

import com.anranruozhu.common.ErrorCode;
import com.anranruozhu.common.ResultUtils;
import com.anranruozhu.exception.BusinessException;
import com.anranruozhu.model.vo.UserVO;
import com.anranruozhu.utils.AlgorithmUtils;
import com.google.gson.reflect.TypeToken;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.anranruozhu.model.domain.User;
import com.anranruozhu.service.UserService;
import com.anranruozhu.mapper.UserMapper;
import com.google.gson.Gson;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.math3.util.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.anranruozhu.constant.UserConstant.*;

/**
* @author anranruozhu
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-06-06 17:32:03
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{
    @Resource
    private UserMapper userMapper;
    @Resource
    private RedisTemplate<Object, Object> redisSession;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        this.updateUserLoginState(request, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 根据标签搜索用户（内存过滤）
     * @param tagNameList 用户要拥有的标签
     * @return User
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        //首先对传入的参数进行判空，防止直接查询所有
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        //sql查询
//        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
//        userMapper.selectOne(queryWrapper);
//        long startTime=System.currentTimeMillis();
//       //QueryWrapper<User> queryWrapper =new QueryWrapper<>();
//        //拼接 and 查询
//        //like '%java%' and '%Python%'
//        for(String tagName : tagNameList){
//            queryWrapper =queryWrapper.like("tags",tagName);
//        }
//        List<User> userList = userMapper.selectList(queryWrapper);
//        log.info("sql query time==" + (System.currentTimeMillis() - startTime));
//        startTime=System.currentTimeMillis();
        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        //1.先查询所有用户
        List<User>userList=userMapper.selectList(queryWrapper);
        //2.再再内存内去进行筛选
        //创建序列化工具类
        Gson gson=new Gson();
//       try{
//           Thread.sleep(3000);
//       }catch(Exception e){
//           log.error("sleep error",e);
//       }
        return userList.stream().filter(user->{
            String tagsStr = user.getTags();
            //因为fuck java的类型擦除机制
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>(){}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for(String tagName:tagNameList){
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }

            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
        //log.info("memory query time==" + (System.currentTimeMillis() - startTime))
    }

    @Override
    public int updateUser(User user,User loginUser) {
        long UserId=user.getId();
        if(UserId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(!isAdmin(loginUser)&&!Objects.equals(user.getId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser=userMapper.selectById(UserId);
        if(oldUser==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if(Boolean.FALSE.equals(redisSession.opsForValue().get(USER_IS_UPDATE+user.getId()))){
            redisSession.opsForValue().set(USER_IS_UPDATE+user.getId(), true);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }else {
            Object ObjectUser = request.getSession().getAttribute(USER_LOGIN_STATE);
            if(ObjectUser==null){
                throw new BusinessException(ErrorCode.NOT_LOGIN);
            }
            User user = (User) ObjectUser;
            if(Boolean.TRUE.equals(redisSession.opsForValue().get(USER_IS_UPDATE+user.getId()))){
                updateUserLoginState(request, user.getId());
            }
            ObjectUser = request.getSession().getAttribute(USER_LOGIN_STATE);
            if(ObjectUser==null){
                throw new BusinessException(ErrorCode.NOT_LOGIN);
            }
            return (User)ObjectUser;
        }
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object ObjectUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(ObjectUser==null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User user = (User)ObjectUser;
        return user.getUserRole().equals(ADMIN_ROLE);
    }

    @Override
    public boolean isAdmin(User loginUser) {
       return loginUser!=null&&loginUser.getUserRole()==ADMIN_ROLE;
    }

    @Override
    public Page<User> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        String redisKey = String.format("yupao:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage;
        // 如果有缓存，直接读缓存
        if(valueOperations.get(redisKey)!=null){
             userPage = (Page<User>) valueOperations.get(redisKey);
        }else{
            // 无缓存，查数据库
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);
            // 写缓存
            try {
                valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error("redis set key error", e);
            }
        }
        return userPage;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 根据标签搜索用户（sql查询版）
     * @param tagNameList 用户要拥有的标签
     * @return User
     */
    @Deprecated
    private List<User> searchUserByTagsBySql(List<String> tagNameList){
        //首先对传入的参数进行判空，防止直接查询所有
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //sql查询
        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        userMapper.selectOne(queryWrapper);
       //QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        //拼接 and 查询
        //like '%java%' and '%Python%'
        for(String tagName : tagNameList){
            queryWrapper =queryWrapper.like("tags",tagName);
        }
        return userMapper.selectList(queryWrapper);
    }

    private void updateUserLoginState(HttpServletRequest request, Long userId) {
        redisSession.delete(USER_IS_UPDATE+userId);
        User user = userMapper.selectById(userId);
        request.getSession().setAttribute(USER_LOGIN_STATE, getSafetyUser(user));
        redisSession.opsForValue().set(USER_IS_UPDATE+user.getId(), false);
    }

    private void updateUserLoginState(HttpServletRequest request, User safetyUser) {
        redisSession.delete(USER_IS_UPDATE+safetyUser.getId());
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        redisSession.opsForValue().set(USER_IS_UPDATE+safetyUser.getId(), false);
    }
}




