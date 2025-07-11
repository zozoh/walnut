package com.site0.walnut.web.setup;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.web.WnConfig;

public class WnCheckRootSetup implements Setup {

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();

        WnConfig conf = ioc.get(WnConfig.class, "conf");

        // 确保有 ROOT 用户
        WnLoginApi auth = ioc.get(WnLoginApi.class, "sysLoginApi");
        String passwd = conf.get("root-init-passwd", "123456");
        auth.addRootUserIfNoExists(passwd);

        // 确保 guest 用户
        auth.addGuestUserIfNoExists();

    }

    @Override
    public void destroy(NutConfig nc) {}

}
