package org.nutz.walnut.ext.sheet;

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
    ARRAY

}
