package com.site0.walnut.ext.util.jsonx.hdl.ttl;

public enum TPLLFieldType {

    /**
     * 数字，通常是正式
     */
    Numeric,

    /**
     * 普通字符
     */
    Character,

    /**
     * 时间类型，精确到纳秒
     * 
     * <pre>
     * 2000 01 01 00 00 00 000000
     * |-- 2000  yyyy
     * |-- 01    MM
     * |-- 01    DD
     * |-- 00    HH
     * |-- 00    mm
     * |-- 00    ss
     * |-- 000   SSS
     * |-- 000   Nano Second
     * </pre>
     */
    Dts20,

    /**
     * 日期类型
     * 
     * <pre>
     * 1901 01 01 
     * |-- 1901  yyyy
     * |-- 01    MM
     * |-- 01    DD
     * </pre>
     * 
     * 其中，<code>00010101</code> 用来表示无限期
     */
    Dcymd8,

    /**
     * 日期时间
     * 
     * <pre>
     * 1901 01 01 12 33 34 
     * |-- 1901  yyyy
     * |-- 01    MM
     * |-- 01    DD
     * |-- 12    HH  
     * |-- 33    mm
     * |-- 34    dd
     * </pre>
     * 
     * 其中，<code>1901 01 01 00 00 00</code> 用来表示无限期
     */
    Dcymd16
}
