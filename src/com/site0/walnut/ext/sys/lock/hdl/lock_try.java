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
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(block)$");
    }

    @Override
    protected void process(WnSystem sys, LockContext fc, ZParams params) {
        String lockName = params.val_check(0);
        String owner = sys.getMyName();
        String hint = params.getString("hint", "USER_ASK");

        long du = params.getLong("du", 3000);
        WnLock lo = null;
        try {
            lo = fc.api.tryLock(lockName, owner, hint, du);
            fc.locks.add(lo);
        }
        catch (WnLockFailException e) {

            // 看看是否是阻塞模式
            boolean blockMode = params.is("block");

            // 对于阻塞模式，需要重复尝试
            if (blockMode) {
                int retryMaxTimes = params.getInt("retry", 3);
                long retryInterval = params.getLong("interval", du);
                int retryCount = 0;

                // 循环尝试加锁
                while (retryCount < retryMaxTimes) {
                    retryCount++;
                    try {
                        lo = fc.api.tryLock(lockName, owner, hint, du);
                        break;
                    }
                    catch (WnLockFailException e1) {
                        fc.api.waitForLockFree(retryInterval);
                    }
                }
            }

            // 加锁成功
            if (null != lo) {
                fc.locks.add(lo);
            }
            // 还是不行，那么输出错误
            else {
                String fail = params.getString("fail");
                if (!Ws.isBlank(fail)) {
                    WnLockObj failLock = Json.fromJson(WnLockObj.class, fail);
                    fc.locks.add(failLock);
                }
            }
        }
    }

}
