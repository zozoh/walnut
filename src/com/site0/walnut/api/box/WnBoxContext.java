package com.site0.walnut.api.box;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;

public class WnBoxContext {

    public WnSession session;

    public WnIo io;

    public WnServiceFactory services;

    public NutMap attrs;

    private WnBoxContext() {}

    public WnBoxContext(WnServiceFactory services, NutMap attrs) {
        this.attrs = attrs;
        this.services = services;
    }

    public WnLoginApi auth() {
        return services.getLoginApi();
    }

    public WnBoxContext clone() {
        WnBoxContext bc = new WnBoxContext();
        bc.attrs = new NutMap();
        bc.services = this.services;
        bc.session = this.session.clone();
        bc.io = this.io;
        bc.attrs.putAll(this.attrs);
        return bc;
    }

}
