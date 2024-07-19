package com.anranruozhu.controller;

import com.anranruozhu.common.BaseResponse;
import com.anranruozhu.common.ErrorCode;
import com.anranruozhu.common.ResultUtils;
import com.anranruozhu.exception.BusinessException;
import com.anranruozhu.model.domain.User;
import com.anranruozhu.model.request.UserLoginRequest;
import com.anranruozhu.model.vo.UserVO;
import com.anranruozhu.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author anranruozhu
 * @ClassName UserController
 * @description
 * @create 2024/6/11 下午1:19
 **/
@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String ,Object> redisTemplate;
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        Page<User> userPage = userService.recommendUsers(pageSize,pageNum,request);
        return ResultUtils.success(userPage);
    }
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList=userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }
    @PostMapping("/update")
    BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request ){
        if(user==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        int Result = userService.updateUser(user,loginUser);

        return ResultUtils.success(Result);
    }
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(userService.getLoginUser(request));
    }
    /**
     * 获取最匹配的用户
     * @param num
     * @param request
     *@retrun
     **/
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if(num<0||num>20){
            throw new  BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num,loginUser));
    }
}
