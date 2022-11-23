package org.nutz.walnut.web.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.walnut.util.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.data.titanium.hdl.ti_webdeps;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnRun;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.web.bean.WnApp;
import org.nutz.web.WebException;

/**
 * 封装系统 应用的相关逻辑
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
public class WnAppService extends WnRun {

    private static final Log log = Wlog.getAPP();

    /**
     * 运行一个命令
     * 
     * @param app
     *            应用
     * @param metaOutputSeparator
     *            写入输出流的宏分隔符，如果为空，则不输出宏指令（这个交给客户端来决定）
     * @param PWD
     *            当前目录路径，默认为 <code>~</code>
     * @param cmdText
     *            运行指令
     * @param out
     *            标准输出流
     * @param err
     *            错误输出流
     * @param ins
     *            标准输入流
     */
    public void runCommand(WnApp app,
                           final String metaOutputSeparator,
                           String PWD,
                           String cmdText,
                           OutputStream out,
                           OutputStream err,
                           InputStream ins) {
        // app 所在目录
        WnObj oAppHome = app.getHome();

        // TODO zozoh: 下这段古老的逻辑应木有用了，找个时间删了吧:
        // // FIXME sudo临时解决方案，防止有人知道sudo，特将命令改为wndo
        // cmdText = cmdText.trim();
        // Matcher sudoM = Regex.getPattern("^wndo[ ]+(.+)$").matcher(cmdText);
        // boolean isSudo = sudoM.find();
        // if (isSudo) {
        // cmdText = sudoM.group(1);
        // if ("root".equals(Wn.WC().checkMyName())) { // root还干啥sudo
        // isSudo = false;
        // }
        // }
        // final WnAuthSession my_se = app.getSession();
        //
        // WnAuthSession su_se = null;
        // if (isSudo) {
        // WnAccount root = auth().checkAccount("root");
        // su_se = auth().createSession(root);
        // }
        //
        // // 运行
        // WnAuthSession se = isSudo ? su_se : my_se;
        // NutMap vars = my_se.getVars();
        // vars.put("PWD", Strings.sBlank(PWD, "~"));
        // vars.put("APP_HOME", oAppHome.path());
        //
        // // 准备变量，以便在回调里释放
        // final WnAuthSession the_su_se = su_se;
        //
        // // 准备命令执行后的回调
        // Writer w = new OutputStreamWriter(out);
        // AppCommandCallback callback = new AppCommandCallback(my_se, w,
        // metaOutputSeparator, () -> {
        // if (the_su_se != null) {
        // auth().removeSession(the_su_se, 0);
        // }
        // });
        // 准备会话变量
        WnAuthSession se = app.getSession();
        NutMap vars = se.getVars();
        vars.put("PWD", Strings.sBlank(PWD, "~"));
        vars.put("APP_HOME", oAppHome.path());

        // 准备命令执行后的回调
        Writer w = new OutputStreamWriter(out);
        AppCommandCallback callback = new AppCommandCallback(se, w, metaOutputSeparator, null);

        // 执行命令
        exec("", se, cmdText, out, err, ins, callback);

    }

    /**
     * 渲染应用的界面前端引导代码
     * 
     * @param app
     *            应用
     * @return 应用的界面 HTML引导代码
     */
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
        // WnObj oInitTmpl = io().fetch(oAppHome, "init_tmpl");
        // if (null != oInitTmpl) {
        // String cmdText = io().readText(oInitTmpl);
        // tmpl = this.exec("app-init-tmpl:", se, appJson, cmdText);
        // }
        // // 否则查找静态模板文件
        // else {
        // tmpl = __find_tmpl(app.getName(), oAppHome);
        // }

        tmpl = __find_tmpl(app.getName(), oAppHome);

        // 如果存在 `init_context` 文件，则执行，将其结果合并到渲染上下文中
        // NutMap map = null;
        // WnObj oInitContext = io().fetch(oAppHome, "init_context");
        // if (null != oInitContext) {
        // String cmdText = io().readText(oInitContext);
        // String contextJson = this.exec("app-init-context:", se, appJson,
        // cmdText);
        // map = Json.fromJson(NutMap.class, contextJson);
        // }
        // 添加自定义的上下文
        // if (null != map)
        // c.putAll(map);

        // 标题
        String title = appName;
        if (null != o)
            title = o.name() + " : " + title;

        // 填充模板占位符
        c.put("title", title);

        // 这些优先级最高
        String rs = conf.get("app-rs", "/gu/rs/");
        c.put("session", se.toMapForClient());
        c.put("rs", rs);
        c.put("appName", appName);
        c.put("app", appJson);
        c.put("appClass", appName.replace('.', '_').toLowerCase());

        // 看看是否需要提供 debug 版
        WnObj oDomain = io().fetch(null, Wn.appendPath(se.getMe().getHomePath(), ".domain"));
        NutMap vars = se.getVars();
        if (null != oDomain && oDomain.getBoolean("debug-app-" + appName.replace('.', '-'))) {
            c.put("TiJs", "ti/core/ti.mjs");
            c.put("WnJs", "ti/lib/walnut/walnut.mjs");
        }
        // 否则设置成 Release 版本
        else {
            // 有木有自定义的 preload呢？
            List<String> preloads = new LinkedList<>();
            preloads.add("@dist:es6/ti-more-all.js");
            String cusPreload = vars.getString("TI_PRELOAD");
            if (!Ws.isBlank(cusPreload)) {
                String[] pres = Ws.splitIgnoreBlank(cusPreload, "[;, ]");
                for (String pl : pres) {
                    preloads.add(pl);
                }
            }

            // 有木有自定义的 DEPS ?
            String depsPaths = vars.getString("TI_DEPS", "/rs/ti/dist/es6/ti-more-all.deps.json");
            String depsUrl = vars.getString("TI_DEPS_URL", "/gu/rs/ti/deps/");
            String depsPrefix = vars.getString("TI_DEPS_PREFIX", "@deps:");
            String depsIgnore = vars.getString("TI_DEPS_IGNORE", null);
            List<NutMap> depsList = ti_webdeps.getWebDepsList(io(),
                                                              depsUrl,
                                                              depsPrefix,
                                                              depsIgnore,
                                                              depsPaths);

            // 生成 HTML
            String depsHtml = ti_webdeps.renderHtml(depsList);

            // 记入上下文变量
            c.put("TiJs", "ti/dist/es6/ti-core.js");
            c.put("WnJs", "ti/dist/es6/ti-walnut.js");
            c.put("preloads", preloads);
            c.put("TiWebDeps", depsHtml);
        }

        // 得到 Theme 的路径
        String theme = vars.getString("THEME", "light");
        c.put("theme", theme);

        // 得到快捷的 theme css 路径
        String themeCss = theme;
        // 确保是一个 css
        if (!themeCss.endsWith(".css")) {
            themeCss += ".css";
        }
        // 这里声明的是一个自定义的全路径主题
        if (themeCss.startsWith("/")) {
            c.put("themeCss", themeCss);
        }
        // 自带主题
        else {
            c.put("themeCss", Wn.appendPath(rs, "ti/theme", themeCss));
        }

        // 渲染视图
        String html = Tmpl.exec(tmpl, c);

        return html;
    }

    /**
     * 获取一个数据对象
     * 
     * @param app
     *            应用
     * @param ph
     *            对象路径。如果是相对路径，则从应用主目录起始查找
     * @return 数据对象，null 表示不存在
     */
    public WnObj getObjByPath(WnApp app, String ph) {
        // 获取会话
        WnAuthSession se = app.getSession();
        // NutMap vars = se.getVars();

        // 当前目录设置为 appHome，这样，str 如果是相对路径，则直接访问应用内文件夹
        // vars.put("PWD", app.getHome().getRegularPath());

        // 默认就是主目录
        if (Strings.isBlank(ph)) {
            return null;
        }
        // 获取对象
        return Wn.checkObj(io(), se, ph);
    }

    /**
     * 获取一个数据对象
     * 
     * @param app
     *            应用
     * @param id
     *            对象的 ID
     * @return 数据对象，null 表示不存在
     */
    public WnObj getObjById(WnApp app, String id) {
        // 默认就是主目录
        if (Strings.isBlank(id)) {
            return null;
        }
        // 获取对象
        return io().checkById(id);
    }

    /**
     * 获取一个数据对象
     * 
     * @param app
     *            应用
     * @param q
     *            对象的查询条件
     * @return 数据对象，null 表示不存在
     */
    public WnObj getObjByQuery(WnApp app, WnQuery q) {
        // 获取会话
        WnAuthSession se = app.getSession();

        // 必须指定查询条件
        if (null == q) {
            return null;
        }
        q.setvToList("d0", "home");
        q.setvToList("d1", se.getMyGroup());
        q.limit(2);

        List<WnObj> list = io().query(q);
        if (list.size() == 2) {
            throw Er.create("e.app.getObjByQuery.MultiObjFound");
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取一个应用。如果不存在，则抛错
     * 
     * @param appName
     *            应用名。形式类似 "xx.xxx" 如果没有前缀会自动补齐 <code>wn.</code>
     * @return 应用对象
     */
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
        WnObj oTmpl = io().fetch(oAppHome, tt + "_tmpl.html");

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
                    oTmpl = io().fetch(null, phTmpl);
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
        String tmpl = io().readText(oTmpl);
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
            try {
                WnObj o = io().fetch(null, ph);
                if (null != o)
                    return o;
            }
            catch (WebException e) {
                log.warn("Fail to fetch " + ph, e);
                if ("e.io.obj.noexists".equals(e.getKey())) {
                    continue;
                }
            }
        }
        return null;
    }
}
