package com.site0.walnut.api.lock;

import java.util.List;

/**
 * 锁服务接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnLockApi {
    
    /**
     * 通知等待的消费线程，有新任务来了
     */
    void notifyWhenLockFree();

    /**
     * 消费线程通过这个方法可以挂起等待
     * 
     * @param waitInMs
     *            挂起等待时长，如果小于等于 0 则根据子类自己理解 譬如 0 可以认为是永久等待， 或者小于等于 0
     *            可以给一个默认值，譬如1分钟
     */
    void waitForLockFree(long waitInMs);

    /**
     * 尝试获取一个锁。如果成功则返回锁对象，如果这个锁被占用，抛出异常
     * <p>
     * 锁对象过期，或者未被使用则会返回锁
     * <p>
     * 本类考虑的不是线程安全性，而是跨越多个物理节点依然安全，不可能让同名的锁被同时取得。 这个需要实现类慎重考虑
     * 
     * @param lockName
     *            锁名，必须全局唯一
     * @param owner
     *            请求者名称
     * @param hint
     *            请求者给这个锁一个线索，譬如因为要写某资源而请求的锁，以便锁拥堵等情况排查问题
     * @param duInMs
     *            请求锁的时长（毫秒）。不同锁服务可以有不同的规定，譬如 0 表示永久锁，或者 0 表示采用默认时长。<br>
     *            实现类也可以自行规定时长的有效值区间等信息
     * @return 锁对象，如果有返回，一定不是 null
     * 
     * @throws WnLockFailException
     *             加锁失败。当前锁已经被占用了
     */
    WnLock tryLock(String lockName, String owner, String hint, long duInMs)
            throws WnLockFailException;

    /**
     * 获取一个锁对象，仅仅是为了运行时的日志之用，获得的锁不能用于释放。<br>
     * 因此随便获取，只是锁对象的 privateKey 字段为空，以防止有些贱人猥琐释放锁。
     * 
     * @param lockName
     *            锁名，必须全局唯一
     * @return 锁对象，如果有返回，null 表示不存在
     */
    WnLock getLock(String lockName);

    /**
     * 尝试释放一个锁对象
     * 
     * @param lock
     *            锁对象
     * 
     * @return 锁对象，如果释放失败，则会返回 null
     * 
     * @throws WnLockInvalidKeyException
     *             释放锁时失败，私钥错误
     * 
     * @see #freeLock(String, String)
     */
    WnLock freeLock(WnLock lock) throws WnLockInvalidKeyException;

    /**
     * 尝试释放一个锁
     * 
     * @param lockName
     *            锁对象名称
     * @param privateKey
     *            锁的私有密码，创建锁的时候你可以拿到
     * 
     * @return 锁对象，如果释放失败，则会返回 null
     * 
     * @throws WnLockInvalidKeyException
     *             释放锁时失败，譬如私钥错误
     */
    WnLock freeLock(String lockName, String privateKey) throws WnLockInvalidKeyException;

    /**
     * @return 当前所有锁的列表
     */
    List<WnLock> list();

}
