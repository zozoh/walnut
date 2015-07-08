package org.nutz.walnut.ext.task.hdl;

import java.util.HashSet;
import java.util.Set;

import org.nutz.walnut.api.io.WnObj;

class OrderUpdateInfo {

    WnObj o;

    Set<String> keys;

    public OrderUpdateInfo(WnObj o) {
        this.o = o;
        this.keys = new HashSet<String>();
    }
}
