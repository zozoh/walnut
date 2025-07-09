package com.site0.walnut.web.bean;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.WnSession;

public class WnApp {

    private String name;

    private WnSession session;

    private WnObj home;

    private WnObj obj;

    public String toJson(JsonFormat jfmt) {
        NutMap map = new NutMap();
        map.put("name", name);
        if (null != session) {
            map.put("session", session.toBean());
        }
        if (null != obj) {
            map.put("obj", obj);
        }
        return Json.toJson(map);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WnObj getHome() {
        return home;
    }

    public void setHome(WnObj home) {
        this.home = home;
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
