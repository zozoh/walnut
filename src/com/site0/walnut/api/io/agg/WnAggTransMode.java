package com.site0.walnut.api.io.agg;

/**
 * 聚集分组的值转换方式
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public enum WnAggTransMode {

    /**
     * 用原始数据聚集
     */
    RAW,

    /**
     * 原始数据是一个时间戳，转换为日期后聚集（按天聚集）
     */
    TIMESTAMP_TO_DATE

}
