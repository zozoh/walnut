package org.nutz.walnut.ext.entity.history;

import org.nutz.dao.QueryResult;

public interface HistoryApi {

    HistoryRecord fetch(String id);

    /**
     * 移除一条或者多条历史记录
     * 
     * @param ids
     *            消息 ID
     * @return 实际移除了多少条消息
     */
    int remove(String... ids);

    /**
     * 根据条件移除多条历史记录
     * 
     * @param q
     *            查询条件
     * @return 实际移除了多少条消息
     */
    int removeBy(HisQuery q);

    /**
     * @param q
     *            查询条件
     * @param pn
     *            当前页（1 base）
     * @param pgsz
     *            页大小
     * @return 查询结果（当前页列表，以及翻页信息）
     */
    QueryResult query(HisQuery q, int pn, int pgsz);

    /**
     * 向消息流里插入一条信息对象
     * 
     * @param his
     *            信息对象
     * @return 插入的信息对象
     */
    HistoryRecord add(HistoryRecord his);

}
