package com.anranruozhu.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author anranruozhu
 * @ClassName PageRequest
 * @description 通用的分页请求的参数
 * @create 2024/7/7 下午3:08
 **/
@Data
public class PageRequest implements Serializable{


    private static final long serialVersionUID = -390561047639461849L;
    /**
     * 页面大小
     */
    protected int pageSize=10;
    /*
    * 当前页面为第几页*
    * */
    protected int pageNum=1;
}
