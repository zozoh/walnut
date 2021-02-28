package org.nutz.walnut.ext.xapi.impl;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.ext.xapi.AbstractThirdXApi;
import org.nutz.walnut.impl.box.WnSystem;

public class WnThirdXApi extends AbstractThirdXApi {

    public WnThirdXApi(WnSystem sys) {
        this(sys.io, sys.session.getVars());
    }

    public WnThirdXApi(WnIo io, WnAuthSession session) {
        this(io, session.getVars());
    }

    public WnThirdXApi(WnIo io, NutBean vars) {
        super();
        this.configs = new WnThirdXConfigManager(this, this.experts, io, vars);
    }

}
