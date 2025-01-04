package com.site0.walnut.impl.lock.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;
import com.site0.walnut.impl.lock.WnLockObj;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class MemoryLockApi implements WnLockApi {

    private Map<String, WnLockObj> locks;

    public MemoryLockApi() {
        this.locks = new HashMap<>();
    }

    @Override
    public void notifyWhenLockFree() {
        Wlang.notifyOne(this);
    }

    @Override
    public void waitForLockFree(long waitInMs) {
        // 怎么也得等个几秒
        if (waitInMs < 0) {
            waitInMs = 1000;
        }
        Wlang.wait(this, waitInMs);
    }

    @Override
    public synchronized WnLock tryLock(String lockName, String owner, String hint, long duInMs)
            throws WnLockFailException {
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
        WnLockObj l2 = lo.clone();
        l2.setPrivateKey(null);
        l2.setName(lockName);
        return l2;
    }

    @Override
    public synchronized WnLock freeLock(WnLock lock) throws WnLockInvalidKeyException {
        return freeLock(lock.getName(), lock.getPrivateKey());
    }

    @Override
    public synchronized WnLock freeLock(String lockName, String privateKey)
            throws WnLockInvalidKeyException {
        // 防空
        if (null == lockName) {
            return null;
        }

        String key = _KEY(lockName);
        WnLockObj lo = locks.get(key);

        // 木有锁
        if (null == lo) {
            return null;
        }
        // 判断一下
        // 校验密码呀失败
        if (!lo.matchPrivateKey(privateKey)) {
            throw new WnLockInvalidKeyException(lo);
        }
        // 移除锁
        locks.remove(key);
        return lo;

    }

    @Override
    public List<WnLock> list() {
        List<WnLock> list = new ArrayList<>(locks.size());
        for (WnLock lo : locks.values()) {
            WnLockObj l2 = (WnLockObj) lo.clone();
            l2.setPrivateKey(null);
            list.add(l2);
        }
        return list;
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
