package com.site0.walnut.api.lock;

/**
 * 释放锁时失败，因为不是相同的一把锁
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnLockInvalidKeyException extends WnLockException {

    public WnLockInvalidKeyException(WnLock lock) {
        super(lock);
    }

}
