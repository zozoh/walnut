package com.site0.walnut.api.lock;

/**
 * 封装一个锁对象
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnLock {

    /**
     * @return 锁名
     */
    String getName();

    /**
     * @return 锁被占用的起始时间（绝对毫秒数）
     */
    long getHoldTime();

    /**
     * @return 锁过期时间（绝对毫秒数）
     */
    long getExpiTime();

    /**
     * @return 当前锁是否过期
     */
    boolean isExpired();

    /**
     * @return 占用者名称
     */
    String getOwner();

    /**
     * @return 占用资源提示
     */
    String getHint();

    /**
     * @return 锁的密钥, 每次加锁成功都会自动变化，以便只有锁的拥有者才能解锁
     */
    String getPrivateKey();

    /**
     * @param lock
     *            另外的锁实例
     * @return 给定锁实例与自己是否是逻辑上相同的一把锁
     */
    boolean isSame(WnLock lock);

}
