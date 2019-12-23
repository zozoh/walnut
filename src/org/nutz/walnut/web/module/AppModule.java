package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.Mvcs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.bean.WnApp;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/a")
public class AppModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    public static View V_304 = new HttpStatusView(304);

    @Filters(@By(type = WnCheckSession.class))
    @At("/open/**")
    @Fail("jsp:jsp.show_text")
    public View open(String appName,
                     @Param("ph") String str,
                     @Param("m") boolean meta,
                     @ReqHeader("If-None-Match") String etag)
            throws UnsupportedEncodingException {

        if (Strings.isBlank(appName))
            return HttpStatusView.HTTP_404;

        // 得到会话对象，以及对应的用户
        WnAuthSession se = Wn.WC().checkSession();
        WnAccount me = Wn.WC().getMe();

        // 如果当前用户的 ID 和名字相等，则必须强迫其改个名字
        if (me.isNameSameAsId()) {
            return new ServerRedirectView("/u/h/rename.html");
        }

        // 如果 appName 没有名称空间，补上 "wn"
        if (appName.indexOf('.') < 0) {
            appName = "wn." + appName;
        }

        // 找到应用
        WnObj oAppHome = this._check_app_home(appName);

        // 得到要处理的对象
        WnObj o = null;
        if (!Strings.isBlank(str)) {
            o = Wn.checkObj(io, se, str);
        }

        // 如果没有指定对象，看看用户有没有设置默认的打开对象
        String dftObjPath = me.getMetaString("DFT_OBJ_PATH");
        if (null == o && !Strings.isBlank(dftObjPath)) {
            o = Wn.checkObj(io, se, dftObjPath);
        }

        // 指定读取元数据
        if (null != o && meta) {
            o.setRWMeta(true);
        }

        // 生成 app 的对象
        WnApp app = new WnApp();
        NutMap seMap = se.toMapForClient();
        app.setObj(o);
        app.setSession(seMap);
        app.setName(appName);

        // .............................................
        // 准备返回视图
        View reView = null;

        // .............................................
        // 后续正常的 APP 打开逻辑
        if (null == reView) {
            reView = __open_page(app, oAppHome, se, etag);
        }

        // 返回输出
        return reView;
    }

    private View __open_page(WnApp app, WnObj oAppHome, WnAuthSession se, String etag) {
        NutMap c = new NutMap();
        String appName = app.getName();
        WnObj o = app.getObj();

        // 这个是要输出的模板
        String tmpl;

        // 检查完毕后，生成 app 的 JSON 描述
        String appJson = Json.toJson(app, JsonFormat.forLook().setQuoteName(true));

        // 如果存在 `init_tmpl` 文件，则执行，将其结果作为模板
        WnObj oInitTmpl = io.fetch(oAppHome, "init_tmpl");
        if (null != oInitTmpl) {
            String cmdText = io.readText(oInitTmpl);
            tmpl = exec("app-init-tmpl:", se, appJson, cmdText);
        }
        // 否则查找静态模板文件
        else {
            tmpl = __find_tmpl(app.getName(), oAppHome);
        }

        // 分析模板

        // 如果存在 `init_context` 文件，则执行，将其结果合并到渲染上下文中
        NutMap map = null;
        WnObj oInitContext = io.fetch(oAppHome, "init_context");
        if (null != oInitContext) {
            String cmdText = io.readText(oInitContext);
            String contextJson = exec("app-init-context:", se, appJson, cmdText);
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
        c.put("session", app.getSession());
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
        String cnt = Tmpl.exec(tmpl, c);
        String sha1 = Lang.sha1(cnt);
        if (etag != null && sha1.equals(etag)) {
            return V_304;
        }
        Mvcs.getResp().setHeader("ETag", sha1);
        return new ViewWrapper(new RawView("html"), cnt);
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

    @Filters(@By(type = WnCheckSession.class, args = {"true"}))
    @At("/load/?/**")
    @Ok("void")
    @Fail("http:404")
    public View load(String appName,
                     String rsName,
                     @Param("mime") String mimeType,
                     @Param("d") String download,
                     @ReqHeader("User-Agent") String ua,
                     @ReqHeader("If-None-Match") String etag,
                     @ReqHeader("Range") String range,
                     HttpServletRequest req,
                     HttpServletResponse resp)
            throws IOException {
        // 准备计时
        Stopwatch sw = null;
        if (log.isDebugEnabled()) {
            log.debugf("APPLoad(%s) : %s", appName, rsName);
            sw = Stopwatch.begin();
        }

        try {
            // 查找 app 的主目录
            WnObj oAppHome = this._check_app_home(appName);

            if (log.isDebugEnabled())
                sw.tag("appHome " + rsName);

            // 读取资源对象
            WnObj o = io.check(oAppHome, rsName);

            // 确保可读，同时处理链接文件
            o = Wn.WC().whenRead(o, false);

            String text = null;
            if (log.isDebugEnabled())
                sw.tag("check_rs " + rsName);

            // 纠正一下下载模式
            ua = WnWeb.autoUserAgent(o, ua, download);

            // 如果是 JSON ，那么特殊的格式化一下
            if ("application/json".equals(mimeType)) {
                NutMap json = Json.fromJson(NutMap.class, text);
                text = Json.toJson(json, JsonFormat.nice());
            }

            // 已经预先处理了内容
            if (null != text) {
                StringInputStream ins = new StringInputStream(text);
                return new WnObjDownloadView(ins,
                                             -1,
                                             ua,
                                             Strings.sBlank(mimeType, o.mime()),
                                             o.name(),
                                             etag,
                                             range);
            }

            // 其他就默认咯
            return new WnObjDownloadView(io, o, ua, etag, range);
        }
        // 最后打印总时长
        finally {
            if (log.isDebugEnabled()) {
                sw.stop();
                log.debugf("APPLoad(%s) : %s DONE %s", appName, rsName, sw);
            }
        }
    }

    @Filters(@By(type = WnCheckSession.class, args = {"true"}))
    @At("/run/**")
    @Ok("void")
    @Fail("ajax")
    public void run(String appName,
                    @Param("mime") String mimeType,
                    @Param("mos") final String metaOutputSeparator,
                    @Param("PWD") String PWD,
                    @Param("cmd") String cmdText,
                    @Param("in") String in,
                    HttpServletRequest req,
                    final HttpServletResponse resp)
            throws IOException {
        // String cmdText = Streams.readAndClose(req.getReader());
        // cmdText = URLDecoder.decode(cmdText, "UTF-8");

        // 找到 app 所在目录
        WnObj oAppHome = this._check_app_home(appName);

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 准备输出
        HttpRespStatusSetter _resp = new HttpRespStatusSetter(resp);
        OutputStream out = new AppRespOutputStreamWrapper(_resp, 200);
        OutputStream err = new AppRespOutputStreamWrapper(_resp, 500);
        InputStream ins = Strings.isEmpty(in) ? null : Lang.ins(in);
        final Writer w = new OutputStreamWriter(out);

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
        final WnAuthSession my_se = Wn.WC().checkSession();

        WnAuthSession su_se = null;
        if (isSudo) {
            WnAccount root = auth().checkAccount("root");
            su_se = auth().createSession(root);
        }

        // 运行
        WnAuthSession se = isSudo ? su_se : my_se;
        my_se.getVars().put("PWD", PWD);
        my_se.getVars().put("APP_HOME", oAppHome.path());

        // 执行命令
        final WnAuthSession the_su_se = su_se;
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

}
