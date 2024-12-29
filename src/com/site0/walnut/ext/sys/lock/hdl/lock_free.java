package com.site0.walnut.ext.sys.lock.hdl;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;
import com.site0.walnut.ext.sys.lock.LockContext;
import com.site0.walnut.ext.sys.lock.LockFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.lock.WnLockObj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class lock_free extends LockFilter {

    @Override
    protected void process(WnSystem sys, LockContext fc, ZParams params) {
        String lockName = params.val_check(0);
        try {
            // 未指定锁，那么似乎用上下的锁对象
            if (Ws.isBlank(lockName)) {
                for (WnLock lo : fc.locks) {
                    WnLock re = fc.api.freeLock(lo);
                    __append_result(fc, lockName, re);
                }
            }
            // 指定了锁
            else {
                String privateKey = params.val_check(1);
                WnLock re = fc.api.freeLock(lockName, privateKey);
                __append_result(fc, lockName, re);
            }
        }
        catch (WnLockInvalidKeyException e) {
            WnLockObj failFo = new WnLockObj();
            failFo.setName(lockName);
            failFo.setHint("Invalid");
            fc.locks.add(failFo);
        }
    }

    private void __append_result(LockContext fc, String lockName, WnLock re) {
        // 失败
        if (null == re) {
            WnLockObj failFo = new WnLockObj();
            failFo.setName(lockName);
            failFo.setHint("Failed");
            fc.locks.add(failFo);
        }
        // 成功
        else {
            fc.locks.add(re);
        }
    }

}
