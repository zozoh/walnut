package org.nutz.walnut.web;

import org.nutz.ioc.Ioc;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrInfo;
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
            String passwd = conf.get("root-init-passwd", "123456");
            root = usrs.create(new WnUsrInfo("root"));
            usrs.setPassword(root, passwd);
            log.infof("init root usr: %s", root.id());
        }
        WnUsr guest = usrs.fetch("guest");
        if (guest == null) {
            String passwd = conf.get("root-init-passwd", R.UU32());
            guest = usrs.create(new WnUsrInfo("guest"));
            usrs.setPassword(guest, passwd);
            log.infof("init guest usr: %s", guest.id());
        }
    }

    @Override
    public void destroy(NutConfig nc) {}

}
