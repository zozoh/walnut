package com.site0.walnut.ext.data.www;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.JvmJsExecContext;
import com.site0.walnut.util.JsExecContext;

public class JvmWnmlRuntime implements WnmlRuntime {

    private WnSystem sys;

    public JvmWnmlRuntime(WnSystem sys) {
        this.sys = sys;
    }

    @Override
    public String readPath(String path) {
        WnObj o = sys.io.check(null, path);
        return sys.io.readText(o);
    }

    @Override
    public String exec2(String cmdText) {
        return sys.exec2(cmdText);
    }

    @Override
    public JsExecContext createJsExecApiContext(StringBuilder sb) {
        return new JvmJsExecContext(sys, sb);
    }

}
