package org.nutz.walnut.core.eot;

import java.util.LinkedList;
import java.util.List;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.walnut.api.io.WnExpiObj;
import org.nutz.walnut.api.io.WnExpiObjTable;
import org.nutz.walnut.api.lock.WnLock;
import org.nutz.walnut.api.lock.WnLockApi;
import org.nutz.walnut.api.lock.WnLockBusyException;
import org.nutz.walnut.api.lock.WnLockFailException;
import org.nutz.walnut.api.lock.WnLockNotSameException;

public class WnSafeExpiObjTable implements WnExpiObjTable {

    private static final Log log = Logs.get();

    private static final String LOCK_NAME = "WN_EXPI_OBJS";

    private WnExpiObjTable table;

    private WnLockApi locks;

    private long tryLockDuration;

    public WnSafeExpiObjTable() {
        this.tryLockDuration = 3000;
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
            lo = locks.tryLock(LOCK_NAME, owner, "takeover_expi_objs", tryLockDuration);
            List<WnExpiObj> list = table.takeover(owner, duInMs, limit);
            return list;
        }
        // 忙锁：系统有点不正常，需要打印一下日志以便追踪问题
        catch (WnLockBusyException e) {
            log.warn("takeover busy to tryLock", e);
            return new LinkedList<>();
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
            catch (WnLockBusyException | WnLockNotSameException e) {
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