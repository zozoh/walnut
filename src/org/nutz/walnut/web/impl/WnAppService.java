package org.nutz.walnut.web.impl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.web.bean.WalnutApp;

public class WnAppService {

    private WnIo io;

    private WnRun run;

    public WnAppService(WnRun run) {
        this.io = run.io();
        this.run = run;
    }

    public String renderAppHtml(WalnutApp app) {
        NutMap c = new NutMap();
        String appName = app.getName();
        WnObj o = app.getObj();

        // 这个是要输出的模板
        String tmpl;

        // 检查完毕后，生成 app 的 JSON 描述
        JsonFormat jfmt = JsonFormat.nice().setQuoteName(true);
        String appJson = app.toJson(jfmt);

        return null;
    }

    public WalnutApp checkApp(String appName, String str) {
        WnAuthSession se = Wn.WC().checkSession();
        // ----------------------------------------
        // 得到要处理的对象
        WnObj o = null;
        // 默认就是主目录
        if (Strings.isBlank(str)) {
            String dftObjPath = se.getVars().getString("DFT_OBJ_PATH", "~");
            o = Wn.checkObj(io, se, dftObjPath);
        }
        // 指定的用户
        else {
            o = Wn.checkObj(io, se, str);
        }
        // ----------------------------------------
        // 如果 appName 没有名称空间，补上 "wn"
        if (appName.indexOf('.') < 0) {
            appName = "wn." + appName;
        }
        // ----------------------------------------
        // 找到应用
        WnObj oAppHome = this._check_app_home(appName);
        // ----------------------------------------
        // 生成 App 对象
        WalnutApp app = new WalnutApp();
        app.setName(appName);
        app.setHome(oAppHome);
        app.setObj(o);
        app.setSession(se);

        return app;
    }

    public WnObj _check_app_home(String appName) {
        WnObj oAppHome = _find_app_home(appName);
        if (null == oAppHome)
            throw Er.create("e.app.noexists", appName);
        return oAppHome;
    }

    protected WnObj _find_app_home(String appName) {
        String appPaths = Wn.WC().checkSession().getVars().getString("APP_PATH");
        String[] bases = Strings.splitIgnoreBlank(appPaths, ":");
        for (String base : bases) {
            String ph = Wn.appendPath(base, appName);
            WnObj o = io.fetch(null, ph);
            if (null != o)
                return o;
        }
        return null;
    }
}
