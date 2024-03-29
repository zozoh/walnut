package com.site0.walnut.api.io;

import java.util.List;

/**
 * 封装了一个读取结构，记录过期对象的键以及其过期时间
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnExpiObjTable {

    /**
     * 插入或者修改一条对象过期记录
     * 
     * @param o
     *            对象过期记录
     */
    void insertOrUpdate(WnExpiObj o);

    /**
     * 插入一条对象的过期记录
     * 
     * @param id
     *            对象 ID
     * @param expi
     *            过期时间
     */
    void insertOrUpdate(String id, long expi);

    /**
     * @param id
     *            对象过期记录 ID
     * @return true 移除成功， false 移除失败或者不存在
     */
    boolean remove(String id);

    /**
     * 请求记录表持有一定数量的对象。
     * <p>
     * 这个操作实现类必须借助系统锁服务，务必保证多线程多节点的情况下，是安全的。 <br>
     * 即，在任意时刻内，集群内任意一个执行者线程，只能有一个真正在执行这个操作。
     * 
     * @param owner
     *            持有者
     * @param duInMs
     *            如果接管了对象，会占用多少毫秒。（负数和0 相当于60000ms）
     * @param limit
     *            最多预期接管多少记录。如果负数或者零，相当于 100
     * 
     * @return 记录列表，顺序一定是最老的在前，较新过期的在后
     */
    List<WnExpiObj> takeover(String owner, long duInMs, int limit);

    /**
     * 删除某所有者，在某个时刻持有的全部记录
     * 
     * @param owner
     *            持有者名称
     * @param hold
     *            持有时间
     * @return 清除了多少条数据
     */
    int clean(String owner, long hold);

}
