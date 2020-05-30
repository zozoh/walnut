package org.nutz.walnut.web;

import org.nutz.ioc.Ioc;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthService;

public class WnCheckRootSetup implements Setup {

    private static final Log log = Logs.get();

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();

        WnConfig conf = ioc.get(WnConfig.class, "conf");

        // 确保有 ROOT 用户
        WnAuthService auth = ioc.get(WnAuthService.class, "sysAuthService");
        WnAccount root = auth.getAccount("root");
        if (root == null) {
            String passwd = conf.get("root-init-passwd", "123456");
            root = new WnAccount("root", passwd);
            auth.createAccount(root);
            log.infof("init root usr: %s", root.getId());
        }
        WnAccount guest = auth.getAccount("guest");
        if (guest == null) {
            String passwd = conf.get("guest-init-passwd", R.UU32());
            guest = new WnAccount("guest", passwd);
            auth.createAccount(guest);
            log.infof("init guest usr: %s", guest.getId());
        }
    }

    @Override
    public void destroy(NutConfig nc) {}

}
