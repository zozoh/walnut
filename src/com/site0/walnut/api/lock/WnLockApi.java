package com.site0.walnut.api.lock;

/**
 * 锁服务接口
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnLockApi {

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
     * @throws WnLockBusyException
     *             当前锁服务繁忙，不能尝试加锁
     * @throws WnLockFailException
     *             加锁失败。当前锁已经被占用了
     */
    WnLock tryLock(String lockName, String owner, String hint, long duInMs)
            throws WnLockBusyException, WnLockFailException;

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
     * @throws WnLockBusyException
     *             当前锁服务繁忙，不能尝试释放锁
     * @throws WnLockNotSameException
     *             释放锁时失败，因为不是相同的一把锁
     */
    void freeLock(WnLock lock) throws WnLockBusyException, WnLockNotSameException;

}