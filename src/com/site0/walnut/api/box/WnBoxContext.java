package com.site0.walnut.api.box;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAuthService;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.io.WnIo;

public class WnBoxContext {

    public WnAuthSession session;

    public WnIo io;

    public WnServiceFactory services;

    public WnAuthService auth;

    public NutMap attrs;

    private WnBoxContext() {}

    public WnBoxContext(WnServiceFactory services, NutMap attrs) {
        this.attrs = attrs;
        this.services = services;
    }

    public WnBoxContext clone() {
        WnBoxContext bc = new WnBoxContext();
        bc.attrs = new NutMap();
        bc.services = this.services;
        bc.session = this.session.clone();
        bc.io = this.io;
        bc.auth = this.auth;
        bc.attrs.putAll(this.attrs);
        return bc;
    }

}
