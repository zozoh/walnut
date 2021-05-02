package org.nutz.walnut.api.box;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;

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
