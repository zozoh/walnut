package org.nutz.walnut.api.box;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;

public class WnBoxContext {

    public WnAuthSession session;

    public WnIo io;

    public WnAuthService auth;

    public NutMap attrs;

    public WnBoxContext(NutMap attrs) {
        this.attrs = attrs;
    }

    public WnBoxContext clone() {
        WnBoxContext bc = new WnBoxContext(new NutMap());
        bc.session = this.session.clone();
        bc.io = this.io;
        bc.auth = this.auth;
        bc.attrs.putAll(this.attrs);
        return bc;
    }

}
