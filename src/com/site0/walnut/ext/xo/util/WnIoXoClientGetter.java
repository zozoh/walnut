package com.site0.walnut.ext.xo.util;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.Wlang;

public class WnIoXoClientGetter<T> implements XoClientGetter<T> {

    private WnIo io;
    private WnObj oHome;
    private String name;

    private XoClientManager<T> man;

    @Override
    public XoClientWrapper<T> get() {
        return man.getClient(io, oHome, name);
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (null == other)
            return false;

        if (!(other instanceof WnIoXoClientGetter<?>)) {
            return false;
        }

        WnIoXoClientGetter<?> get = (WnIoXoClientGetter<?>) other;

        if (!Wlang.isEqual(this.oHome, get.oHome))
            return false;

        if (!Wlang.isEqual(this.name, get.name))
            return false;

        return true;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public void setHome(WnObj oHome) {
        this.oHome = oHome;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setManager(XoClientManager<T> man) {
        this.man = man;
    }

}
