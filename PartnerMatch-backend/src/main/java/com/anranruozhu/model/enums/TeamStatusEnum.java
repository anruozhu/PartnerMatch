package com.anranruozhu.model.enums;

/**
 * @author anranruozhu
 * @ClassName TeamStatusEnum
 * @description 队伍状态的枚举常量
 * @create 2024/7/7 下午4:02
 **/
public enum TeamStatusEnum {
    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");

    private int value;
    private String text;

    public static TeamStatusEnum getEnumByValue(Integer value){

    if(value == null){
        return null;
    }
        TeamStatusEnum[] values = TeamStatusEnum.values();
    for(TeamStatusEnum teamEnum : values){
        if(teamEnum.getValue() == value){
            return teamEnum;
        }
    }
    return null;
    }
    TeamStatusEnum( int value,String text) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
