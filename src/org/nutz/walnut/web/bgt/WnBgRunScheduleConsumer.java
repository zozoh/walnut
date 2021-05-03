package org.nutz.walnut.web.bgt;

import java.util.List;

import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.walnut.api.box.WnServiceFactory;
import org.nutz.walnut.api.lock.WnLockBusyException;
import org.nutz.walnut.api.lock.WnLockFailException;
import org.nutz.walnut.ext.sys.schedule.WnCronSlot;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleApi;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleException;
import org.nutz.walnut.ext.sys.schedule.WnSysScheduleQuery;
import org.nutz.walnut.ext.sys.schedule.cmd_schedule;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.time.WnDayTime;

public class WnBgRunScheduleConsumer implements Runnable {

    private static final Log log = Wlog.getBG_SCHEDULE();

    WnSysScheduleApi scheduleApi;

    public WnBgRunScheduleConsumer(WnServiceFactory sf) {
        this.scheduleApi = sf.getScheduleApi();
    }

    @Override
    public void run() {
        // 进入主循环
        while (!Thread.interrupted()) {
            try {
                // 准备查询对象（仅查当前时间槽）
                WnSysScheduleQuery q = new WnSysScheduleQuery();
                q.setToday(System.currentTimeMillis());
                q.setSlotRange("now");

                List<WnCronSlot> slots = scheduleApi.listSlot(q, true);
                
                if(log.isDebugEnabled()) {
                    log.debug("wakeup");
                }

                // 推入后台任务堆栈
                if (null != slots && !slots.isEmpty()) {
                    log.infof("push %d tasks", slots.size());
                    scheduleApi.pushSchedule(slots, false);
                }

                // 任务执行完毕后看看距离下一个时间槽，要睡多久
                long todayMs = Wtime.todayInMs();
                WnDayTime time = new WnDayTime();
                int slotN = q.getSlotN();
                int slotI = cmd_schedule.timeSlotIndexBySec(time, slotN);
                double unit = (double) 86400 / (double) slotN;
                int sec = (int) (unit * (slotI + 1));
                // 得到下一个时间槽，起始的绝对毫秒数
                long ams = todayMs + (sec * 1000);

                // 那么应该睡多久呢
                long sleepMs = Math.max(1, ams - System.currentTimeMillis());
                log.infof("sleep %dms", sleepMs);
                Wlang.wait(scheduleApi, sleepMs);
            }
            // 看看是不是锁服务的错误
            catch (WnSysScheduleException e) {
                Throwable e2 = e.getCause();
                // 加锁失败，大多数情况是 Cron线程正在搞事情
                // 这个有点巧，所以睡了几秒再试试，应该就能成功
                if (e2 instanceof WnLockFailException) {
                    long ms = R.random(1000, 4000);
                    log.infof("Fail to tryLoack, sleep %dms to retry", ms);
                    Wlang.quiteSleep(ms);
                }
                // 忙锁：大家都很抢啊，多等一会儿
                else if (e2 instanceof WnLockBusyException) {
                    long ms = R.random(3000, 5000);
                    log.infof("Fail to askLoack, sleep %dms to retry", ms);
                    Wlang.quiteSleep(ms);
                }
                // 就是一个退出指令
                else if (e2 != null && e2 instanceof InterruptedException) {
                    break;
                }
                // 其他错误就打印一下
                else {
                    log.warn("Run Schedule Error", e);
                    Wlang.quiteSleep(10);
                }
            }
            catch (Throwable e) {
                Throwable e2 = e.getCause();
                // 就是一个退出指令
                if (e2 != null && e2 instanceof InterruptedException) {
                    break;
                }
                log.warn("Other Error", e);
                Wlang.quiteSleep(10);
            }
        }
        // 退出日志
        log.info("quit safely");
    }

}
