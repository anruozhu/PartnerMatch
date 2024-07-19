package com.anranruozhu.model.request;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author anranruozhu
 * @ClassName TeamUpdateRequest
 * @description 队伍修改业务的请求头
 * @create 2024/7/7 下午4:43
 **/
@Data
public class TeamUpdateRequest implements Serializable {
    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 队伍id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 密码
     */
    private String password;


}
