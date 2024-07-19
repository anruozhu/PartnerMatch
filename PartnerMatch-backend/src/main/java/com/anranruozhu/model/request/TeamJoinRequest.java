package com.anranruozhu.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author anranruozhu
 * @ClassName TeamJoinRequest
 * @description 加入队伍业务的请求头
 * @create 2024/7/7 下午4:43
 **/
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 249708179387642342L;
    /**
     * 队伍id
     */
    private long id;


    /**
     * 密码
     */
    private String password;


}
