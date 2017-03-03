package org.nutz.walnut.ext.www;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;

public class WnSystemWnmlRuntime implements WnmlRuntime {

    private WnSystem sys;

    public WnSystemWnmlRuntime(WnSystem sys) {
        this.sys = sys;
    }

    @Override
    public String readPath(String path) {
        WnObj o = sys.io.check(null, path);
        return sys.io.readText(o);
    }

    @Override
    public String exeCommand(String cmdText) {
        return sys.exec2(cmdText);
    }

}
