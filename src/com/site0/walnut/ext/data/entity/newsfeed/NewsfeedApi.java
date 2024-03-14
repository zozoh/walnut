package com.site0.walnut.ext.data.entity.newsfeed;

import java.util.List;

import org.nutz.dao.QueryResult;

public interface NewsfeedApi {

    Newsfeed fetch(String id);

    void update(Newsfeed feed, String... fields);

    Newsfeed setReaded(String id, boolean readed);

    int setAllReaded(String targetId, boolean readed);

    Newsfeed setStared(String id, boolean stared);

    int cleanAllReaded(String targetId);

    /**
     * @param ids
     *            消息 ID
     * @return 实际移除的记录数量
     */
    int remove(String... ids);

    /**
     * @param q
     *            查询条件
     * @param pn
     *            当前页（1 base）
     * @param pgsz
     *            页大小
     * @return 查询结果（当前页列表，以及翻页信息）
     */
    QueryResult query(FeedQuery q, int pn, int pgsz);

    /**
     * 向消息流里插入一条信息对象
     * 
     * @param feed
     *            信息对象
     * @return 插入的信息对象
     */
    Newsfeed add(Newsfeed feed);

    /***
     * 执行消息的批量插入
     * 
     * @param feed
     *            信息对象模板
     * @param targetIds
     *            目标 ID 列表
     */
    List<Newsfeed> batchAdd(Newsfeed feed, String[] targetIds);

}