package com.site0.walnut.web.setup;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.web.clean.WnIoCleaner;

public class WnCleanExpiredSetup implements Setup {

    private static final Log log = Wlog.getMAIN();

    private Thread _t;

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();
        WnRun _run = ioc.get(WnRun.class);
        _t = new Thread(new WnIoCleaner(_run), "IO_CLEAN");
        _t.start();

        if (log.isInfoEnabled())
            log.info("cleaner start");
    }

    @Override
    public void destroy(NutConfig nc) {
        _t.interrupt();
        if (log.isInfoEnabled())
            log.info("interrupt : " + _t.isInterrupted());
    }

}
