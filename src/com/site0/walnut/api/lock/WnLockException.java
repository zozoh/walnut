package com.site0.walnut.api.lock;

public class WnLockException extends Exception {

    public WnLockException(WnLock lock) {
        this(lock.getName(), lock.getOwner(), lock.getHint());
    }

    public WnLockException(String lockName, String owner, String hint) {
        super(String.format("%s:%s:%s", lockName, owner, hint));
    }

}
