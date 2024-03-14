package com.site0.walnut.web.setup;

import org.nutz.log.Log;
import com.site0.walnut.util.Wlog;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.WnRun;

public class WnRunTestSetup implements Setup {

    private static final Log log = Wlog.getMAIN();

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
