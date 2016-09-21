package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.JspView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.impl.srv.WnActiveCode;
import org.nutz.walnut.impl.srv.WnAppLicenceInfo;
import org.nutz.walnut.impl.srv.WnLicence;
import org.nutz.walnut.impl.srv.WnLicenceService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.bean.WnApp;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/a")
@Filters(@By(type = WnCheckSession.class, args = {"true"}))
public class AppModule extends AbstractWnModule {

    @Inject("java:$conf.get('page-licence-fail', 'licence_fail')")
    private String page_licence_fail;

    @Inject("java:$conf.get('page-licence-gone', 'licence_gone')")
    private String page_licence_gone;

    @Inject("java:$conf.get('page-app', 'app')")
    private String page_app;

    @Inject("refer:licenceService")
    private WnLicenceService licences;

    @Filters(@By(type = WnCheckSession.class))
    @At("/open/**")
    @Fail("jsp:jsp.show_text")
    public View open(String appName, @Param("ph") String str, @Param("m") boolean meta)
            throws UnsupportedEncodingException {

        if (Strings.isBlank(appName))
            return HttpStatusView.HTTP_404;

        // 如果 appName 没有名称空间，补上 "wn"
        if (appName.indexOf('.') < 0) {
            appName = "wn." + appName;
        }

        // 找到应用
        WnObj oAppHome = this._check_app_home(appName);

        // 得到会话对象
        WnSession se = Wn.WC().checkSE();

        // 得到要处理的对象
        WnObj o = null;
        if (!Strings.isBlank(str)) {
            o = Wn.checkObj(io, se, str);
            if (meta)
                o.setRWMeta(true);
        }

        // 生成 app 的对象
        WnApp app = new WnApp();
        NutMap seMap = se.toMapForClient(null);
        app.setObj(o);
        app.setSession(seMap);
        app.setName(appName);

        // .............................................
        // 准备返回视图
        View reView = null;

        // .............................................
        // 检查 Licence 的逻辑
        WnObj oAppLicence = io.fetch(oAppHome, "app_licence");
        if (null != oAppLicence) {
            WnAppLicenceInfo ali = io.readJson(oAppLicence, WnAppLicenceInfo.class);
            reView = __check_licence(app, se, ali);
        }

        // .............................................
        // 如果 null 表示过了许可证检查
        // 后续正常的 APP 打开逻辑
        if (null == reView) {
            reView = __open_page(app, oAppHome, se);
        }

        // 返回输出
        return reView;
    }

    private View __open_page(WnApp app, WnObj oAppHome, WnSession se) {
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
        c.put("session", app.getSession());
        c.put("rs", conf.get("app-rs"));
        c.put("appName", appName);
        c.put("app", appJson);
        c.put("appClass", appName.replace('.', '_').toLowerCase());

        // 渲染视图
        return new ViewWrapper(new JspView("jsp." + page_app), Tmpl.exec(tmpl, c));
    }

    private View __check_licence(WnApp app, WnSession se, WnAppLicenceInfo ali) {
        // 得到有效的激活码
        String clientName = se.group();
        WnActiveCode acode = licences.getActiveCode(ali, clientName);

        // 根据激活码，找到许可证本身
        if (null != acode && !acode.isExpired()) {
            WnLicence licn = licences.getLicence(acode);

            // 没找到许可证那么显示许可证不存在页面
            if (null == licn)
                return new ViewWrapper(new JspView("jsp." + page_licence_gone), ali.toContextMap());

            // 看看是否需要动态判断
            StringBuilder err = new StringBuilder();
            if (licn.hasVerify()) {
                String cmdText = licn.getVerify();
                StringBuilder out = new StringBuilder();
                this.exec("app-licence-check", clientName, null, cmdText, out, err);
            }

            // 嗯，通过验证
            if (err.length() == 0) {
                // 为 app 添加权限字段
                app.setPrivilege(licn.getPrivilege());

                // 总之，返回 null，放过你了
                return null;
            }

            // 添加错误的信息
            ali.setErrMessage(err.toString());
        }
        // 激活码过期或者不存在
        else {
            ali.setErrMessage("e.licence.acode_noexists");
        }

        // 错误，不能通过许可证验证，返回错误视图
        return new ViewWrapper(new JspView("jsp." + page_licence_fail), ali.toContextMap());
    }

    private String __find_tmpl(String appName, WnObj oAppHome) {
        // 找到主界面模板
        String tt = "pc"; // 可以是 "pc" 或者 "mobile"

        WnObj oTmpl = io.fetch(oAppHome, tt + "_tmpl.html");

        // 没有模板则一层层向上寻找
        if (null == oTmpl) {
            String nm = "dft_app_" + tt + "_tmpl.html";
            WnObj p = oAppHome;
            while (null == oTmpl && null != p && !p.isRootNode()) {
                p = p.parent();
                oTmpl = io.fetch(p, nm);
            }
            if (null == oTmpl) {
                throw Er.create("e.app.notemplate", appName);
            }
        }

        // 读取模板并分析
        String tmpl = io.readText(oTmpl);
        return tmpl;
    }

    @At("/load/?/**")
    @Ok("void")
    @Fail("http:404")
    public View load(String appName,
                     String rsName,
                     @Param("mime") String mimeType,
                     @Param("auto_unwrap") boolean auto_unwrap,
                     @ReqHeader("User-Agent") String ua) {
        WnObj oAppHome = this._check_app_home(appName);
        WnObj o = io.check(oAppHome, rsName);
        String text = null;

        // TODO 这个木用，应该删掉，先去掉界面上那坨 var xxx = 就好
        if (auto_unwrap) {
            text = io.readText(o);
            Matcher m = Pattern.compile("^var +\\w+ += *([\\[{].+[\\]}]);$", Pattern.DOTALL)
                               .matcher(text);
            if (m.find()) {
                text = m.group(1);
            }
        }

        // 处理一下 ua 来决定是否下载
        ua = WnWeb.autoUserAgent(o, ua, true);

        // 如果是 JSON ，那么特殊的格式化一下
        if ("application/json".equals(mimeType)) {
            NutMap json = Json.fromJson(NutMap.class, text);
            text = Json.toJson(json, JsonFormat.nice());
        }

        // 已经预先处理了内容
        if (null != text) {
            StringInputStream ins = new StringInputStream(text);
            return new WnObjDownloadView(ins, ins.available(), mimeType);
        }
        // 指定了 mimeType
        else if (!Strings.isBlank(mimeType)) {
            return new WnObjDownloadView(io, o, mimeType, ua);
        }
        // 其他就默认咯
        return new WnObjDownloadView(io, o, ua);
    }

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

        // 运行
        WnSession se = Wn.WC().checkSE();
        se.var("PWD", PWD);
        se.var("APP_HOME", oAppHome.path());

        exec("", se, cmdText, out, err, ins, new Callback<WnBoxContext>() {
            public void invoke(WnBoxContext bc) {
                WnSession se = bc.session;
                if (!Strings.isBlank(metaOutputSeparator))
                    try {
                        w.write("\n" + metaOutputSeparator + ":BEGIN:envs\n");
                        w.write(Json.toJson(se.vars()));
                        w.write("\n" + metaOutputSeparator + ":END\n");
                        w.flush();
                    }
                    catch (IOException e) {
                        throw Lang.wrapThrow(e);
                    }
            }
        });
    }

}
