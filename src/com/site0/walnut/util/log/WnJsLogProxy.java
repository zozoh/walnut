package com.site0.walnut.util.log;

import org.nutz.log.Log;
import org.nutz.log.impl.AbstractLog;
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;

public class WnJsLogProxy {

    private Log log;

    public WnJsLogProxy(Log log) {
        this.log = log;
    }

    public void print(String level, String msg, Object[] args) {
        int lvl = AbstractLog.level(level, true);
        log.log(lvl, msg, args);
    }

    public String getJsErrString(Object error) {
        // 防空
        if (null == error) {
            return "null";
        }
        if (error instanceof ScriptObjectMirror) {
            ScriptObjectMirror som = (ScriptObjectMirror) error;
            Object stackTraceObj = som.get("stack");
            if (null != stackTraceObj) {
                return stackTraceObj.toString();
            }
            return som.toString();
        }
        // 否则不知道是什么消息
        return error.toString();
    }

}
