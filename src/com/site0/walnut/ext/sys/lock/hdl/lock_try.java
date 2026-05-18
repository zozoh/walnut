package com.site0.walnut.ext.sys.lock.hdl;

import org.nutz.json.Json;
import org.nutz.log.Log;

import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.ext.sys.lock.LockContext;
import com.site0.walnut.ext.sys.lock.LockFilter;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.impl.lock.WnLockObj;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

public class lock_try extends LockFilter {

    private static final Log log = Wlog.getCMD();

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(block|preview)$");
    }

    @Override
    protected void process(WnSystem sys, LockContext fc, ZParams params) {
        String val = params.val_check(0);
        String owner = sys.getMyName();
        String hint = params.getString("hint", "USER_ASK");
        // 看看是否是阻塞模式
        boolean blockMode = params.is("block");

        String lockName = fc.normalizeLockName(val);

        // 预览模式
        if (params.is("preview")) {
            WnLockObj plock = fc.api.createLock(lockName, owner, hint);
            fc.locks.add(plock);
            return;
        }

        // 开始尝试加锁
        int duInSec = params.getInt("du", 3);
        long duInMs = duInSec * 1000L;
        WnLock lo = null;

        String traceId = String.format("cmd_lock_try[%s:%s]%ds[%s]",
                                       fc.isGlobleLock() ? "GL" : "Dl",
                                       blockMode ? "block" : "once",
                                       duInSec,
                                       lockName

        );

        if (log.isDebugEnabled()) {
            log.debugf("%s: begin owner=%s, val=%s, hint=%s",
                       traceId,
                       owner,
                       val,
                       hint);
        }

        try {
            lo = fc.api.tryLock(lockName, owner, hint, duInMs);
            fc.locks.add(lo);
            if (log.isDebugEnabled()) {
                log.debugf("%s: success lo=%s", traceId, lo.toString());
            }
        }
        catch (WnLockFailException e) {
            if (log.isDebugEnabled()) {
                log.debugf("%s: fail-1=%s", traceId, e.toString());
            }

            // 对于阻塞模式，需要重复尝试
            if (blockMode) {
                int retryMaxTimes = params.getInt("retry", 3);
                int retryInSec = params.getInt("interval",
                                               Math.min(3, duInSec));
                long retryInterval = retryInSec * 1000L;
                int retryCount = 0;

                if (log.isDebugEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debugf("%s: inter=%d, retryCount=%d, retryMaxTimes=%d",
                                   traceId,
                                   retryInterval,
                                   retryCount,
                                   retryMaxTimes);
                    }
                }

                // 循环尝试加锁
                while (retryCount < retryMaxTimes) {
                    retryCount++;
                    try {
                        lo = fc.api.tryLock(lockName, owner, hint, duInMs);
                        break;
                    }
                    catch (WnLockFailException e1) {
                        if (log.isDebugEnabled()) {
                            if (log.isDebugEnabled()) {
                                log.debugf("%s: fail and wait %dms, retryCount=%d/%d",
                                           traceId,
                                           retryInterval,
                                           retryCount,
                                           retryMaxTimes);
                            }
                        }
                        fc.api.waitForLockFree(retryInterval);
                    }
                }
            }

            // 加锁成功
            if (null != lo) {
                fc.locks.add(lo);
                if (log.isDebugEnabled()) {
                    log.debugf("%s: success lo=%s", traceId, lo.toString());
                }
            }
            // 还是不行，那么输出错误
            else {
                String fail = params.getString("fail");
                if (log.isDebugEnabled()) {
                    log.debugf("%s: fail = %s", traceId, fail);
                }
                if (!Ws.isBlank(fail)) {
                    WnLockObj failLock = Json.fromJson(WnLockObj.class, fail);
                    fc.locks.add(failLock);
                }
            }
        }
    }

}
