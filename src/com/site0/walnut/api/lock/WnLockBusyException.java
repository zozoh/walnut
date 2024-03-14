package com.site0.walnut.api.lock;

/**
 * 当前锁服务繁忙，不能尝试加锁
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLockBusyException extends WnLockException {
    
    public WnLockBusyException(WnLock lock) {
        super(lock);
    }

    public WnLockBusyException(String lockName, String owner, String hint) {
        super(lockName, owner, hint);
    }

}
