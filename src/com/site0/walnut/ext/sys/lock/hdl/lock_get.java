package com.site0.walnut.ext.sys.lock.hdl;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.ext.sys.lock.LockContext;
import com.site0.walnut.ext.sys.lock.LockFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.ZParams;

public class lock_get extends LockFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(strict)$");
    }

    @Override
    protected void process(WnSystem sys, LockContext fc, ZParams params) {
        boolean strictMode = params.is("strict", false);
        for (String lockName : params.vals) {
            WnLock lock = fc.api.getLock(lockName);
            if (null == lock) {
                if (strictMode) {
                    throw Er.create("e.cmd.lock.NoExists", lockName);
                }
                continue;
            }
            fc.locks.add(lock);
        }
    }

}
