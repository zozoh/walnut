package com.site0.walnut.web.clean;

import java.util.Date;

import org.nutz.lang.Each;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Times;
import org.nutz.lang.util.Region;
import org.nutz.log.Log;
import org.nutz.trans.Atom;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnRun;

public class WnIoCleaner implements Atom {

    private static final Log log = Wlog.getBG_CLEARNER();

    WnRun _run;

    public WnIoCleaner(WnRun _run) {
        this._run = _run;
    }

    @Override
    public void run() {
        try {
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
                Wlang.quiteSleep(60*1000);
            }
        }
        catch (InterruptedException e) {
            if (log.isInfoEnabled())
                log.info("------------------- Interrupted & quit");
        }
    }

    private void __in_loop() {
        long now = Wn.now();
        WnQuery q = new WnQuery();
        q.setv("expi", Region.Longf("(,%d]", now));
        q.limit(100);
        while (true) {
            int[] count = new int[1];
            _run.io().each(q, new Each<WnObj>() {
                public void invoke(int index, WnObj o, int length) {
                    if (o.isExpired()) {
                        if (log.isInfoEnabled()) {
                            Date d = new Date(o.expireTime());
                            log.infof("rm expired : %s : %s", Times.sDTms2(d), o.path());
                        }
                        _run.io().delete(o, true);
                        count[0]++;
                    }
                }
            });
            if (count[0] < 100)
                break;
        }
    }

}
