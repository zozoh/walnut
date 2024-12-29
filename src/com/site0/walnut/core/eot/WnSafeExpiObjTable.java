package com.site0.walnut.core.eot;

import java.util.LinkedList;
import java.util.List;

import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.io.WnExpiObj;
import com.site0.walnut.api.io.WnExpiObjTable;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;

public class WnSafeExpiObjTable implements WnExpiObjTable {

    private static final Log log = Wlog.getIO();

    private static final String LOCK_NAME = "WN_EXPI_OBJS";

    private static final String LOCK_HINT = "takeover_expi_objs";

    private WnExpiObjTable table;

    private WnLockApi locks;

    private long tryLockDuration;

    public WnSafeExpiObjTable() {
        this.tryLockDuration = 3000;
    }

    public String toString() {
        return String.format("%s : lock by %s",
                             this.getClass().getName(),
                             locks.getClass().getName());
    }

    @Override
    public void insertOrUpdate(WnExpiObj o) {
        table.insertOrUpdate(o);
    }

    @Override
    public void insertOrUpdate(String id, long expi) {
        table.insertOrUpdate(id, expi);
    }

    @Override
    public boolean remove(String id) {
        return table.remove(id);
    }

    @Override
    public List<WnExpiObj> takeover(String owner, long duInMs, int limit) {
        WnLock lo = null;
        // 尝试加锁
        try {
            lo = locks.tryLock(LOCK_NAME, owner, LOCK_HINT, tryLockDuration);
            List<WnExpiObj> list = table.takeover(owner, duInMs, limit);
            return list;
        }
        // 败锁：没关系，就是取不到咯
        catch (WnLockFailException e) {
            return new LinkedList<>();
        }
        // 确保释放锁
        finally {
            try {
                locks.freeLock(lo);
            }
            catch (WnLockInvalidKeyException e) {
                log.warn("takeover fail to freeLock", e);
            }
        }
    }

    @Override
    public int clean(String owner, long hold) {
        return table.clean(owner, hold);
    }

    public WnExpiObjTable getTable() {
        return table;
    }

    public void setTable(WnExpiObjTable table) {
        this.table = table;
    }

    public WnLockApi getLocks() {
        return locks;
    }

    public void setLocks(WnLockApi locks) {
        this.locks = locks;
    }

    public long getTryLockDuration() {
        return tryLockDuration;
    }

    public void setTryLockDuration(long tryLockDuration) {
        this.tryLockDuration = tryLockDuration;
    }

}
