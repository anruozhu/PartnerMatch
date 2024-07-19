package com.anranruozhu.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author anranruozhu
 * @ClassName PageRequest
 * @description 通用删除请求参数
 * @create 2024/7/7 下午3:08
 **/
@Data
public class DeleteRequest implements Serializable{


    private static final long serialVersionUID = -390561047639461849L;
    /**
     * 要删除的对象的id
     */
    private long id;
}
