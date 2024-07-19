package com.anranruozhu.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author anranruozhu
 * @ClassName UserVO
 * @description 用户封装类（脱敏）
 * @create 2024/7/9 下午3:16
 **/
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 9044370127776529589L;
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


}
