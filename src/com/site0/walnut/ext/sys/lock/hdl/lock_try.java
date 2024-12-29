package com.site0.walnut.ext.sys.lock.hdl;

import org.nutz.json.Json;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.ext.sys.lock.LockContext;
import com.site0.walnut.ext.sys.lock.LockFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.lock.WnLockObj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class lock_try extends LockFilter {

    @Override
    protected void process(WnSystem sys, LockContext fc, ZParams params) {
        String lockName = params.val_check(0);
        String owner = sys.getMyName();
        String hint = params.getString("hint", "USER_ASK");
        String fail = params.getString("fail");
        long du = params.getLong("du", 3000);
        try {
            WnLock lo = fc.api.tryLock(lockName, owner, hint, du);
            fc.locks.add(lo);
        }
        catch (WnLockFailException e) {
            if (!Ws.isBlank(fail)) {
                WnLockObj failLock = Json.fromJson(WnLockObj.class, fail);
                fc.locks.add(failLock);
            }
        }
    }

}
