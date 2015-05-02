package org.nutz.walnut.web;

import org.nutz.ioc.Ioc;
import org.nutz.mvc.NutConfig;
import org.nutz.mvc.Setup;

public class WnSetup implements Setup {

    // private static final Log log = Logs.get();

    @Override
    public void init(NutConfig nc) {
        Ioc ioc = nc.getIoc();
        WnConfig conf = ioc.get(WnConfig.class, "conf");
        nc.setAttribute("rs", conf.getAppRs());
        nc.setAttribute("extrs", conf.get("app-extrs", "/extrs"));
        nc.setAttribute("appnm", conf.get("app-name", "walnut"));

    }

    @Override
    public void destroy(NutConfig nc) {}

}
