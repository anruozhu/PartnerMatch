package com.anranruozhu.model.dto;

import com.anranruozhu.common.PageRequest;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * @author anranruozhu
 * @ClassName TeamQuery
 * @description 队伍查询的请求类
 * @create 2024/7/7 下午2:55
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    /**
     * id
     */
    private Long id;
    /**
     * id列表
     */
    private List<Long> idList;
    /**
     * 搜索关键词（同时对队伍名称和队伍描述进行搜索）
     */
    private String searchText;


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
}
