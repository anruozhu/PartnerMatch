package com.anranruozhu.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 头像Url
     */
    private String avatarUrl;
    /**
     * 性别 0-男，1-女
     */
    private Integer gender;
    /**
     * 密码
     */
    private String userPassword;

    /**
     * 手机号
     */
    private String phone;
    /**
     * 电子邮箱
     */
    private String email;
    /**
     * 用户状态 0-正常
     */
    private Integer userStatus;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 是否被删除
     */
    private Integer isDelete;
    /**
     * 用户角色 0-普通用户，1-管理员
     */
    private Integer userRole;
    /**
     * 星球编号
     */
    private String planetCode;
    /**
     * 标签JSON列表
     */
    private String tags;
    /**
     * 个人简介
     */
    private String profile;

    private static final long serialVersionUID = 1L;
}