package org.nutz.walnut.web.bean;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;

public class WnApp {

    private String name;

    private WnSession session;

    private WnObj obj;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WnSession getSession() {
        return session;
    }

    public void setSession(WnSession session) {
        this.session = session;
    }

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
    }

}
