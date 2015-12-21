package org.nutz.walnut.web;

import org.nutz.ioc.Ioc;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;

public class WnCheckRootSetup implements Setup {

    private static final Log log = Logs.get();

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();

        WnConfig conf = ioc.get(WnConfig.class, "conf");

        // 确保有 ROOT 用户
        WnUsrService usrs = ioc.get(WnUsrService.class, "usrService");
        WnUsr root = usrs.fetch("root");
        if (root == null) {
            root = usrs.create("root", conf.get("root-init-passwd"));
            log.infof("init root usr: %s", root.id());
        }
    }

    @Override
    public void destroy(NutConfig nc) {}

}
