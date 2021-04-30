package org.nutz.walnut.ext.sys.task;

import org.nutz.walnut.api.io.WnObj;

public class WnSysTask {

    public WnObj meta;

    public byte[] input;

    public WnSysTask(WnObj meta, byte[] input) {
        this.meta = meta;
        this.input = input;
    }

    public boolean hasInput() {
        return null != input && input.length > 0;
    }

}
