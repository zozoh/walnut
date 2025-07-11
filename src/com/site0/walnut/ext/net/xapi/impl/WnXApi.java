package com.site0.walnut.ext.net.xapi.impl;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.ext.net.xapi.AbstractThirdXApi;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.util.obj.WnObjGetter;
import com.site0.walnut.util.stream.WnInputStreamFactory;

public class WnXApi extends AbstractThirdXApi {

    private WnObjGetter objGetter;

    public WnXApi(WnSystem sys) {
        this(sys.io, sys.session.getEnv());
        this.objGetter = new WnObjGetter(sys);
    }

    public WnXApi(WnIo io, WnSession session) {
        this(io, session.getEnv());
        this.objGetter = new WnObjGetter(io, session);
    }

    public WnXApi(WnIo io, NutBean vars) {
        super(new WnXApiExpertManager(io, vars));
        this.objGetter = new WnObjGetter(io, vars);
        this.configs = new WnXApiConfigManager(this, this.experts, io, vars);
    }

    public WnXApi(WnIo io) {
        NutMap vars = new NutMap();
        vars.put("HOME", "/root");
        this.experts = new WnXApiExpertManager(io, vars);
        this.objGetter = new WnObjGetter(io, vars);
        this.configs = new WnXApiConfigManager(this, this.experts, io, vars);
    }

    @Override
    public WnInputStreamFactory getInputStreamFactory() {
        return objGetter;
    }

}
