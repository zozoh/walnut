package com.site0.walnut.web.clean;

import java.util.Date;
import java.util.List;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.Times;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import com.site0.walnut.api.io.WnExpiObj;
import com.site0.walnut.api.io.WnExpiObjTable;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;

public class WnExpiObjTableCleaner implements Atom {

    private static final Log log = Wlog.getBG_CLEARNER();

    private String myName;

    private WnExpiObjTable table;

    private WnIo io;

    private long sleepInterval;

    private int cleanLimit;

    private long cleanHold;

    public WnExpiObjTableCleaner(WnExpiObjTable table,
                                 WnIo io,
                                 long sleepInterval,
                                 int cleanLimit,
                                 long cleanHold) {
        this.myName = Wn.getRuntime().getNodeName();
        this.table = table;
        this.io = io;
        this.sleepInterval = sleepInterval;
        this.cleanLimit = cleanLimit;
        this.cleanHold = cleanHold;
    }

    @Override
    public void run() {
        try {
            log.infof("ExpiObjTable(%s) started:\n %s", myName, table.toString());
            log.infof("ExpiObjTable(%s)   - sleepInterval : %d", myName, sleepInterval);
            log.infof("ExpiObjTable(%s)   - cleanLimit    : %d", myName, cleanLimit);
            log.infof("ExpiObjTable(%s)   - cleanHold     : %d", myName, cleanHold);
            while (!Thread.interrupted()) {
                try {
                    __in_loop();
                }
                catch (Throwable e) {
                    Throwable e2 = e.getCause();
                    if (e2 != null && e2 instanceof InterruptedException) {
                        throw (InterruptedException) e2;
                    }
                    if (log.isWarnEnabled())
                        log.warnf("something wrong!", e);
                }
                // 休息一个时间间隔
                Wlang.quiteSleep(sleepInterval);
            }
        }
        catch (InterruptedException e) {
            if (log.isInfoEnabled())
                log.info("ExpiObjTableCleaner Interrupted & quit");
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
                        io.delete(o);
                        count++;
                        lastObjId = oid;
                        log.debugf(" %d. rm %s", i, oid);
                    }
                    // 其实未过期，直接从表格里移除，
                    // 这个场景是: 有人设置了 expi，导致 expiTable 关注了这个对象
                    // 但是，之后他又后悔了，直接将 expi 属性移除
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
            String holdS = Times.format("yyyy-MM-dd HH:mm:ss.SSS", new Date(hold));
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
