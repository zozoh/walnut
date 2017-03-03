package org.nutz.walnut.ext.www;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.WnRun;

public class WnmlModuleRuntime implements WnmlRuntime {

    private WnRun runner;

    private WnSession se;

    public WnmlModuleRuntime(WnRun runner, WnSession se) {
        this.runner = runner;
        this.se = se;
    }

    @Override
    public String readPath(String path) {
        WnObj o = runner.io().check(null, path);
        return runner.io().readText(o);
    }

    @Override
    public String exeCommand(String cmdText) {
        return runner.exec("www", se, cmdText);
    }
}
