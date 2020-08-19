package org.nutz.walnut.web.setup;

import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.util.WnRun;

public class WnRunTestSetup implements Setup {

    private static final Log log = Logs.get();

    @Override
    public void init(NutConfig nc) {

        WnRun run = nc.getIoc().get(WnRun.class);
        WnContext wc = Wn.WC();
        WnAccount me = wc.getMe();

        new Thread(() -> {
            Wn.WC().setMe(me);
            log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            run.exec("test", "root", "date > ~/dateoutput");
            log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        }).start();
        ;

    }

    @Override
    public void destroy(NutConfig nc) {}

}
