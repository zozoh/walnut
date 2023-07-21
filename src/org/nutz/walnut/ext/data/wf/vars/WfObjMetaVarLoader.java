package org.nutz.walnut.ext.data.wf.vars;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnIo;

public class WfObjMetaVarLoader extends WfVarLoader {

    protected WnIo io;

    protected String path;

    public WfObjMetaVarLoader(String varName, WnIo io, String path) {
        super(varName);
        this.io = io;
        this.path = path;
    }

    @Override
    protected NutBean getBean(NutBean vars) {
        return io.check(null, path);
    }

}
