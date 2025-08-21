package com.site0.walnut.ext.xo.builder;

import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.xo.util.XoClientWrapper;
import com.site0.walnut.ext.xo.util.XoClients;

public abstract class AbstractXoClientBuilder<T> implements XoClientBuilder<T> {

    protected WnIo io;
    protected WnObj oHome;
    protected String name;

    protected XoClientWrapper<T> re;

    public AbstractXoClientBuilder(WnIo io, WnObj oHome, String name) {
        this.io = io;
        this.oHome = oHome;
        this.name = name;
        String key = XoClients.genClientKey(oHome, name);
        this.re = createClient(key);
    }

    protected abstract String getTokenPath();

    protected abstract String getConfigPath(String name);

    protected abstract XoClientWrapper<T> createClient(String clientKey);

    public abstract void loadConfig(NutMap props);

}