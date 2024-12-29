package com.site0.walnut.ext.sys.lock.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.ext.sys.lock.LockContext;
import com.site0.walnut.ext.sys.lock.LockFilter;
import com.site0.walnut.impl.box.WnSystem;
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
                    fc.api.freeLock(lo);
                }
            }
            // 指定了锁
            else {
                String privateKey = params.val_check(1);
                fc.api.freeLock(lockName, privateKey);
            }
        }
        catch (Exception e) {
            throw Er.wrap(e);
        }
    }

}
