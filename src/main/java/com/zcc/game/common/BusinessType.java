package com.zcc.game.common;

import org.apache.commons.lang.StringUtils;

/**
 * 收入类型
 */
public enum BusinessType {

    待购买("1"),
    交易中("2"),
    已完成("3"),
    购买中("4")
    ;

    private BusinessType(String value){
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static String getEnumName(String value){
        if(StringUtils.isEmpty(value)) return null;
        BusinessType[] values = BusinessType.values();
        for(int i=0;i<values.length;i++){
            BusinessType enumobj = values[i];
            if(value.equals(enumobj.getValue())){
                return enumobj.name();
            }
        }
        return null;
    }
}
