package org.nutz.walnut.web.setup;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.api.io.WnExpiObjTable;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.web.WnConfig;
import org.nutz.walnut.web.clean.WnExpiObjTableCleaner;

public class WnCleanExpiObjTableSetup implements Setup {

    private static final Log log = Logs.get();

    private Thread _t;

    @Override
    public void init(NutConfig nc) {
        log.info("ExpiObjTable init:");
        Ioc ioc = nc.getIoc();
        WnConfig conf = ioc.get(WnConfig.class, "conf");
        WnIo io = ioc.get(WnIo.class, "rawIo");
        WnExpiObjTable table = ioc.get(WnExpiObjTable.class, "safeExpiObjTable");
        long sleepInterval = conf.getLong("expi-obj-clean-interval", 60000L);
        int cleanLimit = conf.getInt("expi-obj-clean-limit", 100);
        long cleanHold = conf.getLong("expi-obj-clean-hold", sleepInterval);

        WnExpiObjTableCleaner cn = new WnExpiObjTableCleaner(table,
                                                             io,
                                                             sleepInterval,
                                                             cleanLimit,
                                                             cleanHold);
        _t = new Thread(cn, "OBJ_EXPI_CLEANER");
        _t.start();

    }

    @Override
    public void destroy(NutConfig nc) {
        _t.interrupt();
        log.info("expiObjTable interrupt : " + _t.isInterrupted());
    }

}
