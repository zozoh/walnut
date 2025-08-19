package com.site0.walnut.ext.xo.util;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;

public class WnIoXoClientGetter<T> implements XoClientGetter<T> {

    private WnIo io;
    private WnObj oHome;
    private String name;

    private XoClientManager<T> man;

    @Override
    public XoClientWrapper<T> get() {
        return man.getClient(io, oHome, name);
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
