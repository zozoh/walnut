package com.site0.walnut.web2.controller.app;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import com.site0.walnut.util.Ws;
import com.site0.walnut.util.Wdisks;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.log.WnLog;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.xml.CheapXmlParsing;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.web.bean.WnApp;

/**
 * 封装系统 应用的相关逻辑
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppService extends WnRun {

    private static final WnLog log = Wlog.getAPP();

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
        // vars.put("PWD", Ws.sBlank(PWD, "~"));
        // vars.put("APP_HOME", oAppHome.path());
        //
        // // 准备变量，以便在回调里释放
        // final WnAuthSession the_su_se = su_se;
        //
        // // 准备命令执行后的回调
        // Writer w = new OutputStreamWriter(out);
        // WnAppCommandCallback callback = new WnAppCommandCallback(my_se, w,
        // metaOutputSeparator, () -> {
        // if (the_su_se != null) {
        // auth().removeSession(the_su_se, 0);
        // }
        // });
        // 准备会话变量
        WnSession se = app.getSession();
        NutBean vars = se.getEnv();
        String userHomePath = app.getSession().getUser().getHomePath();
        vars.put("PWD", Ws.sBlank(PWD, userHomePath));
        vars.put("APP_HOME", oAppHome.path());

        // 准备命令执行后的回调
        Writer w = new OutputStreamWriter(out);
        WnAppCommandCallback callback = new WnAppCommandCallback(se, w, metaOutputSeparator, null);

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
        // 如果有 index.html 那么就用静态方式渲染
        String html = _render_as_index_html(app);

        // 如果不存在，则默认采用动态模版方式
        if (Ws.isBlank(html)) {
            html = "<html><body>APP without index.html</body></html>";
        }

        return html;
    }

    private String _render_as_index_html(WnApp app) {
        WnObj oIndexHtml = io().fetch(app.getHome(), "index.html");
        if (null == oIndexHtml) {
            return null;
        }
        String html = io().readText(oIndexHtml);

        // 解析一下，替换 <head> 下所有的 script 与 link 的链接
        CheapDocument doc = new CheapDocument("html", "head", "body");
        CheapXmlParsing ing = new CheapXmlParsing(doc);
        ing.parseDoc(html);
        // List<CheapElement> els = doc.findElements(el ->
        // el.isStdTagAs("^(SCRIPT|LINK)$"));
        // String prefix = "/a/load/wn.term";
        // for (CheapElement el : els) {
        // // script
        // if (el.isStdTagAs("SCRIPT")) {
        // String src = el.attr("src");
        // el.attr("src", prefix + src);
        // }
        // // link
        // else if (el.isStdTagAs("LINK")) {
        // String href = el.attr("href");
        // el.attr("href", prefix + href);
        // }
        // }

        // 设置一下 server-config, js 初始化的时候需要这个配置文件
        CheapElement body = doc.body();
        WnSession se = app.getSession();
        WnUser user = se.getUser();
        String dftLoginSite = Wdisks.appendPath(user.getHomePath(), "www/login");
        String loginSite = user.getMetaString("LOGIN_SITE", dftLoginSite);
        body.attr("session-ticket", se.getTicket());
        body.attr("app-name", app.getName());
        body.attr("app-base", "/a/open/" + app.getName());
        body.attr("quit-path", "/a/login/");
        body.attr("domain", user.getMainGroup());
        body.attr("login-site", loginSite);

        // 输出
        return doc.toHtml();
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
        WnSession se = app.getSession();
        // NutMap vars = se.getVars();

        // 当前目录设置为 appHome，这样，str 如果是相对路径，则直接访问应用内文件夹
        // vars.put("PWD", app.getHome().getRegularPath());

        // 默认就是主目录
        if (Ws.isBlank(ph)) {
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
        if (Ws.isBlank(id)) {
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
        WnSession se = app.getSession();

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
     * @param auth
     *            权鉴接口
     * @param appName
     *            应用名。形式类似 "xx.xxx" 如果没有前缀会自动补齐 <code>wn.</code>
     * @return 应用对象
     */
    public WnApp checkApp(String appName, WnLoginApi auth) {
        // 防空
        if (Ws.isBlank(appName))
            throw Er.create("e.app.noname");
        // 获取会话
        WnSession se = Wn.WC().checkSession();

        // ----------------------------------------
        // 找到应用
        WnObj oAppHome = this._check_app_home(appName);

        // ----------------------------------------
        // 生成 App 对象
        WnApp app = new WnApp();
        app.setName(appName);
        app.setHome(oAppHome);
        app.setSession(se);
        app.setAuth(auth);
        return app;
    }

    private WnObj _check_app_home(String appName) {
        WnObj oAppHome = _find_app_home(appName);
        if (null == oAppHome)
            throw Er.create("e.app.noexists", appName);
        return oAppHome;
    }

    private WnObj _find_app_home(String appName) {
        String appPaths = Wn.WC().checkSession().getEnv().getString("APP_PATH");
        String[] bases = Ws.splitIgnoreBlank(appPaths, ":");
        for (String base : bases) {
            String ph = Wn.appendPath(base, appName);
            try {
                WnObj o = io().fetch(null, ph);
                if (null != o)
                    return o;
            }
            catch (RuntimeException e) {
                if (Er.wrap(e) != e) {
                    throw e;
                }
                log.warn("Fail to fetch " + ph, e);
                if ("e.io.obj.noexists".equals(Er.wrap(e).getKey())) {
                    continue;
                }
            }
        }
        return null;
    }
}
