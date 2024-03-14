package com.site0.walnut.ext.media.sheet;

public enum SheetFieldType {

    /**
     * 表示普通字段
     */
    NORMAL,

    /**
     * 日期时间
     */
    DATE,

    /**
     * 数组，会自动拼合成一个半角逗号分隔的字符串
     */
    ARRAY,

    /**
     * 布尔
     */
    BOOLEAN,

    /**
     * 映射
     */
    MAPPING,

    /**
     * 整数
     */
    INT,
    
    /**
     * 长整数
     */
    LONG,
    
    /**
     * 浮点
     */
    FLOAT,
    
    /**
     * 双精度浮点
     */
    DOUBLE,

    /**
     * 字符串
     */
    STR
}
