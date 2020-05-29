package org.nutz.walnut.web.module;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.Ok;
import org.nutz.walnut.WnVersion;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnSysRuntime;

@IocBean
@At("/_")
public class WnPropModule extends AbstractWnModule {

    @At("/version")
    @Ok("jsp:jsp.show_text")
    public String version() {
        return WnVersion.getName();
    }

    @At("/runtime")
    @Ok("json")
    public WnSysRuntime runtime() {
        return Wn.getRuntime();
    }

}
