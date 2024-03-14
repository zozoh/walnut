package com.site0.walnut.web.setup;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import com.site0.walnut.api.io.WnExpiObjTable;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.web.WnConfig;
import com.site0.walnut.web.clean.WnExpiObjTableCleaner;

public class WnCleanExpiObjTableSetup implements Setup {

    private static final Log log = Wlog.getBG_CLEARNER();

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
