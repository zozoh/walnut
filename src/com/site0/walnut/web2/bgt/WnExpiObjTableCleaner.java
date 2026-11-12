package com.site0.walnut.web2.bgt;

import java.util.Date;
import java.util.List;

import com.site0.walnut.api.io.WnExpiObj;
import com.site0.walnut.api.io.WnExpiObjTable;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.log.WnLog;
import com.site0.walnut.log.WnLogs;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnSysRuntime;
import com.site0.walnut.util.Wtimes;

/**
 * 过期对象清理线程。
 * <p>
 * 对应旧 {@code web/clean/WnExpiObjTableCleaner}（RM-NUTZ-15 随 {@code web/} 删除后补回 web2/bgt）。
 * 周期性从 {@link WnExpiObjTable} 接管一批到期记录，删除已过期对象；多节点通过
 * {@code takeover(owner, hold, limit)} 的占用窗口竞争，节点崩溃后由其它节点接管。
 * <p>
 * 线程名 {@code OBJ_EXPI_CLEANER}，由 {@code WnWebBoot} 启动/停止，配置见
 * {@code conf/web.properties} 的 {@code expi-obj-clean-*}。
 */
public class WnExpiObjTableCleaner extends Thread {

    private static final WnLog log = WnLogs.getBG_CLEARNER();

    private final String myName;

    private final WnExpiObjTable table;

    private final WnIo io;

    private final long sleepInterval;

    private final int cleanLimit;

    private final long cleanHold;

    public WnExpiObjTableCleaner(WnExpiObjTable table,
                                 WnIo io,
                                 long sleepInterval,
                                 int cleanLimit,
                                 long cleanHold) {
        super("OBJ_EXPI_CLEANER");
        WnSysRuntime rt = Wn.getRuntime();
        this.myName = null == rt ? "UNKNOWN" : rt.getNodeName();
        this.table = table;
        this.io = io;
        this.sleepInterval = sleepInterval;
        this.cleanLimit = cleanLimit;
        this.cleanHold = cleanHold;
        // 守护线程：destroy 已会 interrupt；万一中断被吞也不阻塞 JVM 退出
        this.setDaemon(true);
    }

    @Override
    public void run() {
        try {
            log.infof("ExpiObjTable(%s) started:\n %s", myName, table.toString());
            log.infof("ExpiObjTable(%s)   - sleepInterval : %d", myName, sleepInterval);
            log.infof("ExpiObjTable(%s)   - cleanLimit    : %d", myName, cleanLimit);
            log.infof("ExpiObjTable(%s)   - cleanHold     : %d", myName, cleanHold);
            while (!isInterrupted()) {
                try {
                    __in_loop();
                }
                catch (Throwable e) {
                    Throwable e2 = e.getCause();
                    if (e2 instanceof InterruptedException) {
                        throw (InterruptedException) e2;
                    }
                    if (log.isWarnEnabled()) {
                        log.warnf("something wrong!", e);
                    }
                }
                // 带锁的 wait，可被 destroy 的 interrupt 立刻唤醒；
                // 用原生 wait 让 InterruptedException 由外层统一捕获（Wlang.wait 会包成运行时异常）
                synchronized (this) {
                    wait(sleepInterval);
                }
            }
        }
        catch (InterruptedException e) {
            if (log.isInfoEnabled()) {
                log.info("ExpiObjTableCleaner Interrupted & quit");
            }
        }
    }

    private void __in_loop() {
        // 首先接手一批
        List<WnExpiObj> list = table.takeover(myName, cleanHold, cleanLimit);

        // 一直清除到没有候选
        while (!list.isEmpty()) {
            log.infof("ExpiObjTable(%s) : takeover %d objs for clean", myName, list.size());
            // 得到自己的 holdTime
            long hold = list.get(0).getHoldTime();

            // 逐个删除
            int i = 0; // 循环计数
            int count = 0; // 真实删除的个数
            String lastObjId = null; // 记录最后一个被删除的 obj ID
            for (WnExpiObj eo : list) {
                String oid = eo.getId();
                WnObj o = io.get(oid);
                if (null != o) {
                    // 过期了就删除
                    if (o.isExpired()) {
                        io.delete(o, true);
                        count++;
                        lastObjId = oid;
                        log.debugf(" %d. rm %s", i, oid);
                    }
                    // 其实未过期，直接从表格里移除
                    // 这个场景是: 有人设置了 expi，之后又直接把 expi 属性移除
                    else {
                        table.remove(oid);
                        log.debugf(" %d. no expired %s", i, oid);
                    }
                }
                // 迷失的对象，打印一下
                else if (log.isInfoEnabled()) {
                    log.infof(" %d. noexist : %s", i, oid);
                }
                // 计数
                i++;
            }
            String holdS = Wtimes.format("yyyy-MM-dd HH:mm:ss.SSS", new Date(hold));
            log.infof("ExpiObjTable(%s) : remove %d of %d objs(%s) at %s",
                      myName,
                      count,
                      i,
                      lastObjId,
                      holdS);

            int cc = table.clean(myName, hold);
            log.infof("ExpiObjTable(%s) : clean table %d records", myName, cc);

            // 让 CPU 缓缓
            Wlang.quiteSleep(100);

            // 再接手一批
            list = table.takeover(myName, sleepInterval, cleanLimit);
        }
    }

}
