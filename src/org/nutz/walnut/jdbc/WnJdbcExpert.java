package org.nutz.walnut.jdbc;

public interface WnJdbcExpert {

    /**
     * 如何将时间戳转换为日期
     * 
     * @param key
     *            时间戳字段名
     * @return 一个函数调用，将指定时间戳字段转换为日期对象
     */
    String funcTimestampToDate(String key);

    /**
     * 计数函数怎么调用
     * 
     * @param key
     *            字段名
     * @return 聚集函数（计数）
     */
    String funcAggCount(String key);

    /**
     * 求最大值函数怎么调用
     * 
     * @param key
     *            字段名
     * @return 聚集函数（最大值）
     */
    String funcAggMax(String key);

    /**
     * 求最小值函数怎么调用
     * 
     * @param key
     *            字段名
     * @return 聚集函数（最小值）
     */
    String funcAggMin(String key);

    /**
     * 计算平均数函数怎么调用
     * 
     * @param key
     *            字段名
     * @return 聚集函数（平均数）
     */
    String funcAggAvg(String key);

    /**
     * 求和函数怎么调用
     * 
     * @param key
     *            字段名
     * @return 聚集函数（求和）
     */
    String funcAggSum(String key);

}
