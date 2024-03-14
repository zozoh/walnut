package com.site0.walnut.api.lock;

/**
 * 释放锁时失败，因为不是相同的一把锁
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLockNotSameException extends WnLockException {

    public WnLockNotSameException(WnLock lock) {
        super(lock);
    }

    public WnLockNotSameException(String lockName, String owner, String hint) {
        super(lockName, owner, hint);
    }

}
