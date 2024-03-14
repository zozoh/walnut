package com.site0.walnut.api.lock;

/**
 * 加锁失败。当前锁已经被占用了
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLockFailException extends WnLockException {
    
    public WnLockFailException(WnLock lock) {
        super(lock);
    }

    public WnLockFailException(String lockName, String owner, String hint) {
        super(lockName, owner, hint);
    }

}
