package com.site0.walnut.ext.data.wf.vars;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WfObjContentVarLoader extends WfObjMetaVarLoader {

    public WfObjContentVarLoader(String varName, WnIo io, String path) {
        super(varName, io, path);
    }

    @Override
    protected NutBean getBean(NutBean vars) {
        WnObj obj = io.check(null, path);
        NutMap map = io.readJson(obj, NutMap.class);
        return map;
    }

}
