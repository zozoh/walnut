package org.nutz.walnut.web.impl;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.web.bean.WalnutApp;

@IocBean
public class WnAppService extends WnRun {

    public String renderAppHtml(WalnutApp app) {
        NutMap c = new NutMap();
        String appName = app.getName();
        WnObj o = app.getObj();
        WnObj oAppHome = app.getHome();
        WnAuthSession se = app.getSession();

        // 这个是要输出的模板
        String tmpl;

        // 检查完毕后，生成 app 的 JSON 描述
        JsonFormat jfmt = JsonFormat.nice().setQuoteName(true);
        String appJson = app.toJson(jfmt);

        // 如果存在 `init_tmpl` 文件，则执行，将其结果作为模板

        WnObj oInitTmpl = io.fetch(oAppHome, "init_tmpl");
        if (null != oInitTmpl) {
            String cmdText = io.readText(oInitTmpl);
            tmpl = this.exec("app-init-tmpl:", se, appJson, cmdText);
        }
        // 否则查找静态模板文件
        else {
            tmpl = __find_tmpl(app.getName(), oAppHome);
        }

        // 如果存在 `init_context` 文件，则执行，将其结果合并到渲染上下文中
        NutMap map = null;
        WnObj oInitContext = io.fetch(oAppHome, "init_context");
        if (null != oInitContext) {
            String cmdText = io.readText(oInitContext);
            String contextJson = this.exec("app-init-context:", se, appJson, cmdText);
            map = Json.fromJson(NutMap.class, contextJson);
        }

        // 标题
        String title = appName;
        if (null != o)
            title = o.name() + " : " + title;

        // 填充模板占位符
        c.put("title", title);

        // 添加自定义的上下文
        if (null != map)
            c.putAll(map);

        // 这些优先级最高
        String rs = conf.get("app-rs", "/gu/rs");
        c.put("session", se.toMapForClient());
        c.put("rs", rs);
        c.put("appName", appName);
        c.put("app", appJson);
        c.put("appClass", appName.replace('.', '_').toLowerCase());

        // 得到 Theme 的路径
        String theme = se.getVars().getString("THEME", "light");
        if (!theme.endsWith(".css")) {
            theme += ".css";
        }
        // 这里声明的是一个自定义的全路径主题
        if (theme.startsWith("/")) {
            c.put("theme", theme);
        }
        // 自带主题
        else {
            c.put("theme", Wn.appendPath(rs, "ti/theme", theme));
        }

        // 渲染视图
        String html = Tmpl.exec(tmpl, c);

        return html;
    }

    public WalnutApp checkApp(String appName, String str) {
        // 防空
        if (Strings.isBlank(appName))
            throw Er.create("e.app.noname");
        // 获取会话
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

    private String __find_tmpl(String appName, WnObj oAppHome) {
        // 找到主界面模板
        String tt = "pc"; // 可以是 "pc" 或者 "mobile"

        // 首先看看有木有自定义的模板
        WnObj oTmpl = io.fetch(oAppHome, tt + "_tmpl.html");

        // 没有模板，那么从所有的 APP_PATH 里寻找
        if (null == oTmpl) {
            // 一层层向上寻找
            String nmTmpl = "dft_app_" + tt + "_tmpl.html";

            // 在所有的 APP_PATH 里寻找
            if (null == oTmpl) {
                String appPaths = Wn.WC().checkSession().getVars().getString("APP_PATH");
                String[] bases = Strings.splitIgnoreBlank(appPaths, ":");
                for (String base : bases) {
                    String phTmpl = Wn.appendPath(base, nmTmpl);
                    oTmpl = io.fetch(null, phTmpl);
                    if (null != oTmpl)
                        break;
                }
            }

            // 还是没有，就抛错
            if (null == oTmpl) {
                throw Er.create("e.app.notemplate", appName);
            }
        }

        // 读取模板并分析
        String tmpl = io.readText(oTmpl);
        return tmpl;
    }

    private WnObj _check_app_home(String appName) {
        WnObj oAppHome = _find_app_home(appName);
        if (null == oAppHome)
            throw Er.create("e.app.noexists", appName);
        return oAppHome;
    }

    private WnObj _find_app_home(String appName) {
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
