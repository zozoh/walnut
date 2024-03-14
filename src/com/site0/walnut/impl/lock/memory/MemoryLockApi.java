package com.site0.walnut.impl.lock.memory;

import java.util.HashMap;
import java.util.Map;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockBusyException;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.api.lock.WnLockNotSameException;
import com.site0.walnut.impl.lock.WnLockObj;
import com.site0.walnut.util.Wn;

public class MemoryLockApi implements WnLockApi {

    private Map<String, WnLockObj> locks;

    public MemoryLockApi() {
        this.locks = new HashMap<>();
    }

    @Override
    public synchronized WnLock tryLock(String lockName, String owner, String hint, long duInMs)
            throws WnLockBusyException, WnLockFailException {
        // 那么尝试获取锁
        String key = _KEY(lockName);
        WnLockObj lo = locks.get(key);

        // 木有锁
        if (null == lo) {
            return this.setLock(lockName, owner, hint, duInMs);
        }
        // 锁已经过期
        else if (lo.isExpired()) {
            return this.setLock(lockName, owner, hint, duInMs);
        }
        // 锁还是有效的，
        throw new WnLockFailException(lockName, owner, hint);
    }

    @Override
    public synchronized WnLock getLock(String lockName) {
        String key = _KEY(lockName);
        WnLockObj lo = locks.get(key);

        // 木有锁
        if (null == lo) {
            return null;
        }
        // 去掉 privateKey 以防小人作祟
        // 对方因为不知道 privateKey，所以没法 free 这个锁
        lo = lo.clone();
        lo.setPrivateKey(null);
        lo.setName(lockName);
        return lo;
    }

    @Override
    public synchronized void freeLock(WnLock lock)
            throws WnLockBusyException, WnLockNotSameException {
        // 防守
        if (null == lock) {
            return;
        }
        String lockName = lock.getName();
        if (null == lockName) {
            return;
        }

        // 那么尝试获取锁
        String key = _KEY(lockName);
        WnLockObj lo = locks.get(key);

        // 木有锁
        if (null == lo) {
            return;
        }
        // 判断一下
        else {
            lo = lo.clone();
            lo.setName(lockName);
            // 不是自己的锁
            if (!lo.isSame(lock)) {
                throw new WnLockNotSameException(lock);
            }
            // 移除锁
            locks.remove(key);
        }

    }

    private WnLock setLock(String lockName, String owner, String hint, long duInMs) {
        String key = _KEY(lockName);
        long now = Wn.now();
        long expi = now + duInMs;
        WnLockObj lo = new WnLockObj();
        lo.setHoldTime(now);
        lo.setExpiTime(expi);
        lo.setOwner(owner);
        lo.setHint(hint);
        lo.genPrivateKey();
        lo.setName(lockName);
        locks.put(key, lo);
        return lo.clone();
    }

    private String _KEY(String lockName) {
        return lockName;
    }
}
