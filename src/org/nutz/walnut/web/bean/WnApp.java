package org.nutz.walnut.web.bean;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;

public class WnApp {

    private String name;

    private NutMap session;

    private WnObj obj;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NutMap getSession() {
        return session;
    }

    public void setSession(NutMap session) {
        this.session = session;
    }

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
    }

}
