package com.site0.walnut.ext.sys.schedule.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.lock.WnLock;
import com.site0.walnut.api.lock.WnLockApi;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.api.lock.WnLockInvalidKeyException;
import com.site0.walnut.ext.sys.cron.WnSysCron;
import com.site0.walnut.ext.sys.schedule.WnCronSlot;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleException;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleQuery;
import com.site0.walnut.ext.sys.schedule.bean.WnMinuteSlotIndex;
import com.site0.walnut.ext.sys.task.WnSysTask;
import com.site0.walnut.ext.sys.task.WnSysTaskException;
import com.site0.walnut.ext.sys.task.impl.WnSafeSysTaskService;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;

public class WnSafeSysScheduleService implements WnSysScheduleApi {

    private static final Log log = Wlog.getCMD();

    private static final String LOCK_NAME = "WN_SYS_SCHEDULE";

    private static final String LOCK_HINT_LOAD = "load_sys_schedule";

    private static final String LOCK_HINT_PUSH = "push_sys_schedule";

    private static final String LOCK_HINT_CLEAN = "clean_sys_schedule";

    private Object taskNotifyLock;

    private WnSysScheduleApi schedules;

    private WnLockApi locks;

    private long tryLockDuration;

    private long tryAddTaskLockDuration;

    @Override
    public List<WnSysTask> pushSchedule(List<WnCronSlot> slots, boolean keep)
            throws WnSysTaskException, WnSysScheduleException {
        // 防守
        if (null == slots || slots.isEmpty()) {
            return new LinkedList<>();
        }
        // 准备节点信息
        String nodeName = Wn.getRuntime().getNodeName();
        WnLock loSchedule = null;
        WnLock loTask = null;
        // 尝试加锁，要加两个锁
        try {
            // 自己的锁，时间通常比较长，先加上
            loSchedule = locks.tryLock(LOCK_NAME, nodeName, LOCK_HINT_PUSH, tryLockDuration);

            // 尝试获取系统任务堆栈的锁。这里申请锁的有效期任务服务类要长一些
            // 因为可能有比较多的任务需要一次性插入
            try {
                loTask = locks.tryLock(WnSafeSysTaskService.LOCK_NAME,
                                       nodeName,
                                       LOCK_HINT_PUSH,
                                       tryAddTaskLockDuration);
            }
            // 败锁：没关系，就是取不到咯
            catch (WnLockFailException e) {
                log.warn("sysScheduleApi.pushSchedule fail to tryTaskLock", e);
                throw new WnSysScheduleException(e);
            }

            return schedules.pushSchedule(slots, keep);
        }
        // 败锁：没关系，就是取不到咯
        catch (WnLockFailException e) {
            log.warn("sysScheduleApi.pushSchedule fail to tryLock", e);
            throw new WnSysScheduleException(e);
        }
        // 确保释放锁
        finally {
            try {
                locks.freeLock(loTask);
            }
            catch (WnLockInvalidKeyException e) {
                log.warn("sysScheduleApi.pushSchedule fail to freeTaskLock", e);
                throw new WnSysScheduleException(e);
            }
            try {
                locks.freeLock(loSchedule);
            }
            catch (WnLockInvalidKeyException e) {
                log.warn("sysScheduleApi.pushSchedule fail to freeScheduleLock", e);
                throw new WnSysScheduleException(e);
            }
            // 最后，尝试一下通知任务处理线程
            Wlang.notifyAll(this.taskNotifyLock);
        }
    }

    @Override
    public List<WnObj> cleanSlotObj(WnSysScheduleQuery query) throws WnSysScheduleException {
        String nodeName = Wn.getRuntime().getNodeName();
        WnLock lo = null;
        // 尝试加锁
        try {
            lo = locks.tryLock(LOCK_NAME, nodeName, LOCK_HINT_CLEAN, tryLockDuration);
            return schedules.cleanSlotObj(query);
        }
        // 败锁：没关系，就是取不到咯
        catch (WnLockFailException e) {
            log.warn("sysScheduleApi.cleanSlotObj fail to tryLock", e);
            throw new WnSysScheduleException(e);
        }
        // 确保释放锁
        finally {
            try {
                if (null != lo) {
                    locks.freeLock(lo);
                }
            }
            catch (WnLockInvalidKeyException e) {
                log.warn("sysScheduleApi.cleanSlotObj fail to freeLock", e);
                throw new WnSysScheduleException(e);
            }
        }
    }

    @Override
    public List<WnMinuteSlotIndex> loadSchedule(List<WnSysCron> list,
                                                Date today,
                                                String slot,
                                                int amount,
                                                boolean force)
            throws WnSysScheduleException {
        // 防守
        if (null == list || list.isEmpty()) {
            return new LinkedList<>();
        }
        // 准备节点信息
        String nodeName = Wn.getRuntime().getNodeName();
        WnLock lo = null;
        // 尝试加锁
        try {
            lo = locks.tryLock(LOCK_NAME, nodeName, LOCK_HINT_LOAD, tryLockDuration);
            return schedules.loadSchedule(list, today, slot, amount, force);
        }
        // 败锁：没关系，就是取不到咯
        catch (WnLockFailException e) {
            log.warn("sysScheduleApi.loadSchedule fail to tryLock", e);
            throw new WnSysScheduleException(e);
        }
        // 确保释放锁
        finally {
            try {
                if (null != lo) {
                    locks.freeLock(lo);
                }
            }
            catch (WnLockInvalidKeyException e) {
                log.warn("sysScheduleApi.loadSchedule fail to freeLock", e);
                throw new WnSysScheduleException(e);
            }
        }
    }

    @Override
    public List<WnObj> listSlotObj(WnSysScheduleQuery query, boolean loadContent) {
        return schedules.listSlotObj(query, loadContent);
    }

    @Override
    public List<WnCronSlot> listSlot(WnSysScheduleQuery query, boolean loadContent) {
        return schedules.listSlot(query, loadContent);
    }

}
