package com.site0.walnut.ext.data.sqlx.trans;

import java.sql.Connection;
import java.util.Date;

import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.Wuu;

public class RdsTransItem {
    /**
     * 对象唯一标识ID，相当于是一个 UU32
     */
    private String transId;

    /**
     * 数据连接对象
     */
    private Connection conn;
    /**
     * 过期时间（秒）
     */
    private int timeout;

    /**
     * 本实例的创建时间（时间戳）
     */
    private long createdAt;

    /**
     * 本实例的过期时间（时间戳）
     */
    private long expiredAt;

    /**
     * 创建一个事务持有对象
     * 
     * @param conn
     *            数据库连接
     * @param timeout
     *            过期时间（秒） <code>60</code>表示60秒过期
     */
    public RdsTransItem(Connection conn, int timeout) {
        this.transId = Wuu.UU32();
        this.conn = conn;
        this.timeout = timeout;
        this.createdAt = updateExpiredAt(timeout);

    }

    /**
     * 根据指定的 timeout 以及当前系统时间更新过期时间戳
     * 
     * @param timeout
     *            过期时间（秒）
     * @return 函数执行时刻的时间戳
     */
    public long updateExpiredAt(int timeout) {
        long now = System.currentTimeMillis();
        this.expiredAt = now + timeout * 1000;
        return now;
    }

    public String toString() {
        Date ct = new Date(this.createdAt);
        Date expi = new Date(this.expiredAt);
        return String.format("RdsTrans(%s) timeout %sSec. created: %s, expired: %s",
                             transId,
                             timeout,
                             Wtime.formatDateTime(ct),
                             Wtime.formatDateTime(expi));
    }

    /**
     * 判断本对象是否相对于给定时间戳是过期的。 通常这个判断是在一个循环里做的，因此会预先统一获得系统时间戳
     * 
     * @param now
     *            系统当前时间戳
     * @return 是否过期
     */
    public boolean isExpiredFor(long now) {
        return now > this.expiredAt;
    }

    public String getTransId() {
        return transId;
    }

    public Connection getConn() {
        return conn;
    }

    public int getTimeout() {
        return timeout;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

}
