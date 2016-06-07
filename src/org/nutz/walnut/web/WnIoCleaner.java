package org.nutz.walnut.web;

import java.util.Date;

import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Region;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;

public class WnIoCleaner implements Atom {

    private static final Log log = Logs.get();
    
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
                catch (Exception e) {
                    Throwable e2 = e.getCause();
                    if (e2 != null && e2 instanceof InterruptedException) {
                        throw (InterruptedException) e2;
                    }
                    if (log.isWarnEnabled())
                        log.warnf("something wrong!", e);
                }
                // 休息一个时间间隔
                Thread.sleep(60000);
                Lang.wait(Wn.class, 60000);
            }
        }
        catch (InterruptedException e) {
            if (log.isInfoEnabled())
                log.info("------------------- Interrupted & quit");
        }
    }

    private void __in_loop() {
        long now = System.currentTimeMillis();
        WnQuery q = new WnQuery();
        q.setv("expi", Region.Longf("(,%d]", now));
        WnUsr usr = _run.usrs().check("root");
        WnSession se = _run.sess().create(usr);
        _run.io().each(q, new Each<WnObj>() {
            public void invoke(int index, WnObj o, int length) {
                if (o.isExpired()) {
                    if (log.isInfoEnabled()) {
                        Date d = new Date(o.expireTime());
                        log.infof("rm expired : %s : %s", Times.sDTms2(d), o.path());
                    }
                    _run.runWithHook(se, usr, "root", null, new Callback<WnSession>() {
                        public void invoke(WnSession se) {
                            _run.io().delete(o, true);
                        }
                    });
                }
            }
        });
    }

}
