package org.nutz.walnut.web;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.api.io.WnIo;

public class WnCleanExpiredSetup implements Setup {

    private static final Log log = Logs.get();

    private Thread _t;

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();
        WnIo io = ioc.get(WnIo.class, "io");
        _t = new Thread(new WnIoCleaner(io), "IO_CLEAN");
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
