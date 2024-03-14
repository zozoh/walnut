package com.site0.walnut.util.obj;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.WnObjCache;

public class WnObjJoinLoader {

    private WnIo io;

    private NutBean vars;

    private WnObjJoinFields join;

    private WnObjCache cache;

    public WnObjJoinLoader(WnIo io, NutBean vars, WnObjJoinFields join, WnObjCache cache) {
        this.io = io;
        this.vars = vars;
        this.join = join;
        this.cache = cache;
    }

    public WnObjJoinLoader(WnIo io, NutBean vars) {
        this(io, vars, null, null);
    }

    public WnObjJoinLoader(WnSystem sys) {
        this(sys.io, sys.session.getVars(), null, null);
    }

    public WnObjJoinLoader(WnSystem sys, WnObjJoinFields join, WnObjCache cache) {
        this(sys.io, sys.session.getVars(), join, cache);
    }

    public void loadMeta(WnObj o, NutBean meta) {
        if (null == join || !join.isJoinable()) {
            return;
        }
        // TODO
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public NutBean getVars() {
        return vars;
    }

    public void setVars(NutBean vars) {
        this.vars = vars;
    }

    public WnObjJoinFields getJoin() {
        return join;
    }

    public void setJoin(WnObjJoinFields join) {
        this.join = join;
    }

    public WnObjCache getCache() {
        return cache;
    }

    public void setCache(WnObjCache cache) {
        this.cache = cache;
    }

}
