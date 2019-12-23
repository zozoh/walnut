package org.nutz.walnut.web.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.regex.Matcher;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.web.bean.WnApp;

@IocBean
public class WnAppService extends WnRun {

    public void runCommand(WnApp app,
                           final String metaOutputSeparator,
                           String PWD,
                           String cmdText,
                           OutputStream out,
                           OutputStream err,
                           InputStream ins) {
        // app 所在目录
        WnObj oAppHome = app.getHome();

        // FIXME sudo临时解决方案，防止有人知道sudo，特将命令改为wndo
        cmdText = cmdText.trim();
        Matcher sudoM = Regex.getPattern("^wndo[ ]+(.+)$").matcher(cmdText);
        boolean isSudo = sudoM.find();
        if (isSudo) {
            cmdText = sudoM.group(1);
            if ("root".equals(Wn.WC().checkMyName())) { // root还干啥sudo
                isSudo = false;
            }
        }
        final WnAuthSession my_se = app.getSession();

        WnAuthSession su_se = null;
        if (isSudo) {
            WnAccount root = auth().checkAccount("root");
            su_se = auth().createSession(root);
        }

        // 运行
        WnAuthSession se = isSudo ? su_se : my_se;
        my_se.getVars().put("PWD", PWD);
        my_se.getVars().put("APP_HOME", oAppHome.path());

        // 准备变量，以便在回调里释放
        final WnAuthSession the_su_se = su_se;

        // 执行命令
        final Writer w = new OutputStreamWriter(out);
        exec("", se, cmdText, out, err, ins, new Callback<WnBoxContext>() {
            @Override
            public void invoke(WnBoxContext bc) {
                WnAuthSession se = my_se; // 强制使用原来的se
                // 有宏的分隔符，表示客户端可以接受更多的宏命令
                if (!Strings.isBlank(metaOutputSeparator)) {
                    try {
                        // 无论怎样，都设置环境变量
                        w.write("\n"
                                + metaOutputSeparator
                                + ":MACRO:"
                                + Wn.MACRO.UPDATE_ENVS
                                + "\n");
                        w.write(Json.toJson(se.getVars()));
                        w.flush();
                        // 修改当前客户端的 session
                        if (bc.attrs.has(Wn.MACRO.CHANGE_SESSION)) {
                            String json = Json.toJson(bc.attrs.get(Wn.MACRO.CHANGE_SESSION),
                                                      JsonFormat.compact());
                            w.write("\n"
                                    + metaOutputSeparator
                                    + ":MACRO:"
                                    + Wn.MACRO.CHANGE_SESSION
                                    + "\n");
                            w.write(json);
                            w.flush();
                        }
                    }
                    catch (IOException e) {
                        throw Lang.wrapThrow(e);
                    }
                }
                if (the_su_se != null) {
                    auth().removeSession(the_su_se, 0);
                }
            }
        });

    }

    public String renderAppHtml(WnApp app) {
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

    public WnObj getObj(WnApp app, String str) {
        // 获取会话
        WnAuthSession se = app.getSession();
        NutMap vars = se.getVars();

        // 当前目录设置为 appHome
        vars.put("PWD", app.getHome().getRegularPath());

        // 默认就是主目录
        if (Strings.isBlank(str)) {
            return null;
        }
        // 指定的用户
        return Wn.checkObj(io, se, str);
    }

    public WnApp checkApp(String appName) {
        // 防空
        if (Strings.isBlank(appName))
            throw Er.create("e.app.noname");
        // 获取会话
        WnAuthSession se = Wn.WC().checkSession();

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
        WnApp app = new WnApp();
        app.setName(appName);
        app.setHome(oAppHome);
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
