package com.site0.walnut.util.log;

import org.nutz.log.Log;
import org.nutz.log.impl.AbstractLog;

public class WnJsLogProxy {

    private Log log;

    public WnJsLogProxy(Log log) {
        this.log = log;
    }

    public void print(String level, String msg, Object[] args) {
        int lvl = AbstractLog.level(level, true);
        log.log(lvl, msg, args);
    }
}
