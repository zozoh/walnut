package org.nutz.walnut.ext.net.xapi.impl;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.ext.net.xapi.AbstractThirdXApi;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.obj.WnObjGetter;
import org.nutz.walnut.util.stream.WnInputStreamFactory;

public class WnThirdXApi extends AbstractThirdXApi {

    private WnObjGetter objGetter;

    public WnThirdXApi(WnSystem sys) {
        this(sys.io, sys.session.getVars());
        this.objGetter = new WnObjGetter(sys);
    }

    public WnThirdXApi(WnIo io, WnAuthSession session) {
        this(io, session.getVars());
        this.objGetter = new WnObjGetter(io, session);
    }

    public WnThirdXApi(WnIo io, NutBean vars) {
        super(new WnThridXExpertManager(io, vars));
        this.objGetter = new WnObjGetter(io, vars);
        this.configs = new WnThirdXConfigManager(this, this.experts, io, vars);
    }

    @Override
    public WnInputStreamFactory getInputStreamFactory() {
        return objGetter;
    }

}
