package com.site0.walnut.core.io;

import org.nutz.trans.Atom;
import org.nutz.trans.Proton;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

/**
 * 多线程竞争写一个文件对象的时候，会出现问题 因此写对象（包括修改元数据）都需要加全局锁
 * 
 * 再 WrapperIo 的写函数，用这个类封装一下加锁逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnIoWriteLocker {

    private WnLockApi locks;

    private String lockName;

    private String myName;

    private String hint;

    public WnIoWriteLocker(WnLockApi locks, WnObj obj, String[] operations, String methodName) {
        this.locks = locks;
        this.myName = Wn.WC().checkMyName();
        this.hint = "io_" + methodName;
        this._set_lock_name(obj.id());
    }

    public WnIoWriteLocker(WnLockApi locks, String objId, String[] operations, String methodName) {
        this.locks = locks;
        this.myName = Wn.WC().checkMyName();
        this.hint = "io_" + methodName;
        this._set_lock_name(objId);
    }

    private void _set_lock_name(String objId) {
        if (null == objId || Ws.isBlank(objId)) {
            this.lockName = null;
        } else {
            this.lockName = "write:" + objId;
        }
    }

    public <T> T safeReturn(Proton<T> proton) {
        safeRun(proton);
        return proton.get();
    }

    public void safeRun(Atom atom) {
        if (null == lockName) {
            return;
        }
        while (true) {
            try {
                WnLock lock = tryLock();
                atom.run();
                locks.freeLock(lock);
                Wlang.notifyAll(WnIoWriteLocker.class);
                break;
            }
            // 加锁失败的话，需要等待一会而
            catch (WnLockFailException e) {
                e.printStackTrace();
                Wlang.wait(WnIoWriteLocker.class, 3000);
            }
            // 这应该是不可能的
            catch (WnLockInvalidKeyException e) {
                throw Er.wrap(e);
            }
        }
    }

    public WnLock tryLock() throws WnLockFailException {
        return locks.tryLock(lockName, myName, hint, 60000);
    }

}
