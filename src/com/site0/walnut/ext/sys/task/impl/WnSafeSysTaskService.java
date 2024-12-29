package com.site0.walnut.ext.sys.task.impl;

import java.io.InputStream;
import java.util.List;

import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.WnAuthExecutable;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockBusyException;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;
import com.site0.walnut.ext.sys.task.WnSysTask;
import com.site0.walnut.ext.sys.task.WnSysTaskApi;
import com.site0.walnut.ext.sys.task.WnSysTaskException;
import com.site0.walnut.ext.sys.task.WnSysTaskQuery;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class WnSafeSysTaskService implements WnSysTaskApi {

    private static final Log log = Wlog.getCMD();

    public static final String LOCK_NAME = "WN_SYS_TASKS";

    private static final String LOCK_HINT_ADD = "add_sys_task";

    private static final String LOCK_HINT_REMOVE = "remove_sys_task";

    private static final String LOCK_HINT_POP = "pop_sys_task";

    private WnSysTaskApi tasks;

    private WnLockApi locks;

    private long tryLockDuration;

    public void runTask(WnAuthExecutable runer, WnObj oTask, WnAccount user, InputStream input)
            throws WnSysTaskException {
        tasks.runTask(runer, oTask, user, input);
    }

    @Override
    public WnSysTask addTask(WnObj oTask, byte[] input) throws WnSysTaskException {
        String nodeName = Wn.getRuntime().getNodeName();
        WnLock lo = null;
        // 尝试加锁
        try {
            lo = locks.tryLock(LOCK_NAME, nodeName, LOCK_HINT_ADD, tryLockDuration);
            return tasks.addTask(oTask, input);
        }
        // 忙锁：系统有点不正常，需要打印一下日志以便追踪问题
        catch (WnLockBusyException e) {
            log.warn("sysTaskApi.addTask busy to tryLock", e);
            throw new WnSysTaskException(e);
        }
        // 败锁：没关系，就是取不到咯
        catch (WnLockFailException e) {
            log.warn("sysTaskApi.addTask fail to tryLock", e);
            throw new WnSysTaskException(e);
        }
        // 确保释放锁
        finally {
            try {
                locks.freeLock(lo);
            }
            catch (WnLockBusyException | WnLockInvalidKeyException e) {
                log.warn("sysTaskApi.addTask fail to freeLock", e);
                throw new WnSysTaskException(e);
            }
            // 最后，尝试一下通知任务处理线程
            Wlang.notifyAll(this);
        }
    }

    @Override
    public void removeTask(WnObj oTask) throws WnSysTaskException {
        String nodeName = Wn.getRuntime().getNodeName();
        WnLock lo = null;
        // 尝试加锁
        try {
            lo = locks.tryLock(LOCK_NAME, nodeName, LOCK_HINT_REMOVE, tryLockDuration);
            tasks.removeTask(oTask);
        }
        // 忙锁：系统有点不正常，需要打印一下日志以便追踪问题
        catch (WnLockBusyException e) {
            log.warn("sysTaskApi.removeTask busy to tryLock", e);
            throw new WnSysTaskException(e);
        }
        // 败锁：没关系，就是取不到咯
        catch (WnLockFailException e) {
            log.warn("sysTaskApi.removeTask fail to tryLock", e);
            throw new WnSysTaskException(e);
        }
        // 确保释放锁
        finally {
            try {
                locks.freeLock(lo);
            }
            catch (WnLockBusyException | WnLockInvalidKeyException e) {
                log.warn("sysTaskApi.removeTask fail to freeLock", e);
                throw new WnSysTaskException(e);
            }
        }
    }

    @Override
    public WnSysTask popTask(WnSysTaskQuery query) throws WnSysTaskException {
        String nodeName = Wn.getRuntime().getNodeName();
        WnLock lo = null;
        // 尝试加锁
        try {
            lo = locks.tryLock(LOCK_NAME, nodeName, LOCK_HINT_POP, tryLockDuration);
            return tasks.popTask(query);
        }
        // 忙锁：系统有点不正常，需要打印一下日志以便追踪问题
        catch (WnLockBusyException e) {
            log.warn("sysTaskApi.removeTask busy to tryLock", e);
            throw new WnSysTaskException(e);
        }
        // 败锁：没关系，就是取不到咯
        catch (WnLockFailException e) {
            log.warn("sysTaskApi.removeTask fail to tryLock", e);
            throw new WnSysTaskException(e);
        }
        // 确保释放锁
        finally {
            try {
                locks.freeLock(lo);
            }
            catch (WnLockBusyException | WnLockInvalidKeyException e) {
                log.warn("sysTaskApi.removeTask fail to freeLock", e);
                throw new WnSysTaskException(e);
            }
        }
    }

    @Override
    public WnObj checkTask(String id) {
        return tasks.checkTask(id);
    }

    @Override
    public List<WnObj> listTasks(WnSysTaskQuery query) {
        return tasks.listTasks(query);
    }

}
