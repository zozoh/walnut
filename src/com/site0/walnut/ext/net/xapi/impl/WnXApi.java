package com.site0.walnut.ext.net.xapi.impl;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.xapi.AbstractThirdXApi;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.obj.WnObjGetter;
import com.site0.walnut.util.stream.WnInputStreamFactory;

public class WnXApi extends AbstractThirdXApi {

    private WnObjGetter objGetter;

    public WnXApi(WnSystem sys) {
        this(sys.io, sys.session.getVars());
        this.objGetter = new WnObjGetter(sys);
    }

    public WnXApi(WnIo io, WnAuthSession session) {
        this(io, session.getVars());
        this.objGetter = new WnObjGetter(io, session);
    }

    public WnXApi(WnIo io, NutBean vars) {
        super(new WnXApiExpertManager(io, vars));
        this.objGetter = new WnObjGetter(io, vars);
        this.configs = new WnXApiConfigManager(this, this.experts, io, vars);
    }

    @Override
    public WnInputStreamFactory getInputStreamFactory() {
        return objGetter;
    }

}
