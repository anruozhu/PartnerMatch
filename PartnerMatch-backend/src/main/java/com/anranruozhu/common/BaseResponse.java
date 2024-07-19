package com.anranruozhu.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author anranruozhu
 * @ClassName BaseResponse
 * @description 通用返回类
 * @create 2024/6/11 下午1:20
 **/
@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private T data;
    private String message;
    private String description;

public BaseResponse(int code,T data,String message,String description){
    this.code=code;
    this.data=data;
    this.message=message;
    this.description=description;
}
public BaseResponse(int code,T data,String message){
    this(code,data,message,"");
}
public BaseResponse(int code,T data){
    this(code,data,"","");
}
public BaseResponse(ErrorCode errorCode){
    this(errorCode.getCode(),null,errorCode.getMessage(),errorCode.getDescription());
}

}
