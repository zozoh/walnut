package org.nutz.walnut.api.io.agg;

public enum WnAggMode {

    /**
     * 用原始数据聚集
     */
    RAW,

    /**
     * 原始数据是一个时间戳，转换为日期后聚集（按天聚集）
     */
    TIMESTAMP_TO_DATE

}
