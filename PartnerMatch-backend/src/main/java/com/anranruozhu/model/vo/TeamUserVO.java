package com.anranruozhu.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author anranruozhu
 * @ClassName TeamUserVO
 * @description 队伍信息和用户封装类（脱敏 ）
 * @create 2024/7/9 下午3:13
 **/
@Data
public class TeamUserVO implements Serializable {
    private static final long serialVersionUID = 1240041491842630170L;
    /**
     * id
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
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id(队长 id)
     */
    private Long userId;

    /**
     * 0 - 公,1 - 私有,2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;
    /**
     * 队伍创建人
     */
    private UserVO creatUser;
    /**
     * 已经加入的人数
     */
    private Integer hasJoinNum;
    /**
     * 用户是否加入该队伍
     */
    boolean hasJoin=false;

}
