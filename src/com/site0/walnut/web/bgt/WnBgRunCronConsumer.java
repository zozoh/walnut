package com.site0.walnut.web.bgt;

import java.util.List;

import org.nutz.lang.random.R;
import org.nutz.log.Log;
import com.site0.walnut.api.box.WnServiceFactory;
import com.site0.walnut.api.lock.WnLockBusyException;
import com.site0.walnut.api.lock.WnLockFailException;
import com.site0.walnut.ext.sys.cron.WnSysCron;
import com.site0.walnut.ext.sys.cron.WnSysCronApi;
import com.site0.walnut.ext.sys.cron.WnSysCronQuery;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleApi;
import com.site0.walnut.ext.sys.schedule.WnSysScheduleException;
import com.site0.walnut.ext.sys.schedule.cmd_schedule;
import com.site0.walnut.ext.sys.schedule.bean.WnMinuteSlotIndex;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.time.WnDayTime;

public class WnBgRunCronConsumer implements Runnable {

    private static final Log log = Wlog.getBG_CRON();

    WnSysCronApi cronApi;
    WnSysScheduleApi scheduleApi;

    /**
     * 每次最多填充多少个分钟槽
     */
    private int amount;

    public WnBgRunCronConsumer(WnServiceFactory sf, int amount) {
        this.cronApi = sf.getCronApi();
        this.scheduleApi = sf.getScheduleApi();
        this.amount = amount;
    }

    @Override
    public void run() {
        // 准备定期表达式的查询条件
        WnSysCronQuery q = new WnSysCronQuery();
        q.setLimit(1000);

        // 为了不和其他线程抢锁，先随机睡一点时间
        long aheadSleep = R.random(3000, 5000);
        Wlang.quiteSleep(aheadSleep);

        // 进入主循环
        while (!Thread.interrupted()) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("wakeup");
                }

                // 查询系统中的全部定期任务
                List<WnSysCron> crons = cronApi.listCron(q, true);

                // 得到当前时间槽下标
                int sI = cmd_schedule.timeSlotIndex("now", 1440);
                if (crons.size() > 0) {
                    WnDayTime t = new WnDayTime(sI * 60);
                    log.infof("slot[%s][%s] %d tasks", sI, t, crons.size());
                } else if (log.isDebugEnabled()) {
                    WnDayTime t = new WnDayTime(sI * 60);
                    log.debugf("slot[%s][%s] 0 tasks", sI, t);
                }

                // 加载对应的分钟槽
                String sIs = "" + sI;
                List<WnMinuteSlotIndex> list = scheduleApi.loadSchedule(crons,
                                                                        null,
                                                                        sIs,
                                                                        amount,
                                                                        false);

                // 为了节省日志输出，将当前分钟槽前的索引都设置为空
                if (!list.isEmpty()) {
                    WnMinuteSlotIndex firstSlotIndex = list.get(0);
                    for (int i = 0; i < sI; i++) {
                        firstSlotIndex.removeSlot(i);
                    }
                    String siContent = Ws.join(list, "---------\n");
                    if (log.isDebugEnabled()) {
                        log.debug(siContent);
                    }
                }

                // 任务执行完毕后看看距离下一个波时间槽，要睡多久，提前一个时间槽醒来
                long todayMs = Wtime.todayInMs();
                int slotN = 1440;
                double unit = (double) 86400 / (double) slotN;
                int sec = (int) (unit * (sI + amount - 1));
                // 得到下一个时间槽，起始的绝对毫秒数
                long ams = todayMs + (sec * 1000);

                // 那么应该睡多久呢，至少要睡一个时间槽周期吧
                long sleepMs = Math.max(ams - System.currentTimeMillis(), (long) unit);
                if (log.isDebugEnabled()) {
                    log.debugf("sleep %dms", sleepMs);
                }
                Wlang.quiteSleep(sleepMs);
            }
            // 看看是不是锁服务的错误
            catch (WnSysScheduleException e) {
                Throwable e2 = e.getCause();
                // 加锁失败，大多数情况是 Schedule 线程正在搞事情
                // 这个有点巧，所以睡了几秒再试试，应该就能成功
                if (e2 instanceof WnLockFailException) {
                    long ms = R.random(3000, 5000);
                    log.infof("Fail to tryLoack, sleep %dms to retry", ms);
                    Wlang.quiteSleep(ms);
                }
                // 忙锁：大家都很抢啊，多等一会儿
                else if (e2 instanceof WnLockBusyException) {
                    long ms = R.random(1000, 2000);
                    log.infof("Fail to askLoack, sleep %dms to retry", ms);
                    Wlang.quiteSleep(ms);
                }
                // 就是一个退出指令
                else if (e2 != null && e2 instanceof InterruptedException) {
                    break;
                }
                // 其他错误就打印一下
                else {
                    log.warn("Run Cron Error", e);
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
