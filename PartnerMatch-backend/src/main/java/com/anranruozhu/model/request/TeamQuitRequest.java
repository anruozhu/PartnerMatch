package com.anranruozhu.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author anranruozhu
 * @ClassName TeamQuitRequest
 * @description 退出队伍业务的请求头
 * @create 2024/7/7 下午4:43
 **/
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = -7404366071614503146L;
    /**
     * 队伍id
     */
    private long teamId;


}
