package com.site0.walnut.api.io;

/**
 * 封装了一个对象过期记录
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnExpiObj {

    /**
     * @return 对象 ID（主键去重）
     */
    String getId();

    /**
     * @return 对象过期时间
     */
    long getExpiTime();

    /**
     * @return 被某清理线程占用的过期时间，超过这个时间，其他清理线程才可操作对象
     */
    long getHoldTime();

    /**
     * @return 被哪个清理线程占用
     */
    String getOwner();

}
