package com.anranruozhu.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author anranruozhu
 * @ClassName UserLoginRequest
 * @description 用户登陆请求类
 * @create 2024/6/11 下午9:14
 **/
@Data
public class UserLoginRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    private String userAccount;

    private String userPassword;
}
