package com.site0.walnut.ext.sys.task;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Ws;

public class WnSysTask {

    public WnObj meta;

    public byte[] input;

    public String toString() {
        String userName = meta.getString("user");
        String command = Ws.trim(meta.getString("command"));
        if (meta.isType("cron")) {
            String cron = meta.getString("cron");
            return String.format("cron<%s>:%s:%s", userName, cron, command);
        }
        return String.format("task[%s]<%s>:%s", meta.name(), userName, command);
    }

    public WnSysTask(WnObj meta, byte[] input) {
        this.meta = meta;
        this.input = input;
    }

    public boolean hasInput() {
        return null != input && input.length > 0;
    }

}
