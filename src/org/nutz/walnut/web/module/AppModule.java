package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.adaptor.QueryStringAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.view.JspView;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnSession;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.bean.WnApp;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/a")
@Filters(@By(type = WnCheckSession.class))
public class AppModule extends AbstractWnModule {

    @Inject("java:$conf.get('page-app', 'app')")
    private String page_app;

    @At("/open/**")
    @Fail("jsp:jsp.show_text")
    public View open(String str, @Param("m") boolean meta) throws UnsupportedEncodingException {
        str = Strings.trim(str);
        str = URLDecoder.decode(str, "UTF-8");

        // 分析
        int pos = str.indexOf(':');
        String appName;
        if (pos > 0) {
            appName = str.substring(0, pos);
            str = Strings.trim(str.substring(pos + 1));
        } else {
            appName = str;
            str = null;
        }

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
        if (!Strings.isEmpty(str)) {
            str = URLDecoder.decode(str, "UTF-8");
            o = Wn.checkObj(io, se, str);
            if (meta)
                o.setRWMeta(true);
            // 看看是否需要重定向一下
            if (!str.startsWith("~") && !str.startsWith("/") && !str.startsWith("id:")) {
                String url = "/a/open/" + appName + ":id:" + o.id();
                if (meta || o.isRWMeta()) {
                    url += "?m=true";
                }
                return new ServerRedirectView(url);
            }
        }

        // 生成 app 的对象
        WnApp app = new WnApp();
        app.setObj(o);
        app.setSession(Wn.WC().checkSE());
        app.setName(appName);

        // 这个是 app 的 JSON 描述
        String appJson = Json.toJson(app, JsonFormat.forLook().setQuoteName(true));

        // 临时设置一下当前目录
        se.env("PWD", oAppHome.path());

        // 这个是要输出的模板
        String tmpl;

        // 如果存在 `init_tmpl` 文件，则执行，将其结果作为模板
        WnObj oInitTmpl = io.fetch(oAppHome, "init_tmpl");
        if (null != oInitTmpl) {
            String cmdText = io.readText(oInitTmpl);
            tmpl = _run_cmd("app-init-tmpl:", se, appJson, cmdText);
        }
        // 否则查找静态模板文件
        else {
            tmpl = __find_tmpl(appName, oAppHome);
        }

        // 分析模板
        Segment seg = Segments.create(tmpl);

        // 如果存在 `init_context` 文件，则执行，将其结果合并到渲染上下文中
        NutMap map = null;
        WnObj oInitContext = io.fetch(oAppHome, "init_context");
        if (null != oInitContext) {
            String cmdText = io.readText(oInitContext);
            String contextJson = _run_cmd("app-init-context:", se, appJson, cmdText);
            map = Json.fromJson(NutMap.class, contextJson);
        }

        // 标题
        String title = appName;
        if (null != o)
            title = o.name() + " : " + title;

        // 填充模板占位符
        Context c = Lang.context();
        c.set("title", title);
        c.set("rs", conf.get("app-rs"));
        c.set("appName", appName);
        c.set("app", appJson);
        c.set("appClass", appName.replace('.', '_').toLowerCase());
        if (null != map)
            c.putAll(map);

        // 渲染输出
        return new ViewWrapper(new JspView("jsp." + page_app), seg.render(c));
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
                     @Param("auto_unwrap") boolean auto_unwrap) {
        WnObj oAppHome = this._check_app_home(appName);
        WnObj o = io.check(oAppHome, rsName);
        String text = null;

        if (auto_unwrap) {
            text = io.readText(o);
            Matcher m = Pattern.compile("^var +\\w+ += *([\\[{].+[\\]}]);$", Pattern.DOTALL)
                               .matcher(text);
            if (m.find()) {
                text = m.group(1);
            }
        }

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
            return new WnObjDownloadView(io, o, mimeType);
        }
        // 其他就默认咯
        return new WnObjDownloadView(io, o);
    }

    @AdaptBy(type = QueryStringAdaptor.class)
    @At("/run/?/**")
    @Ok("void")
    public void run(String appName,
                    String mimeType,
                    @Param("mos") final String metaOutputSeparator,
                    HttpServletRequest req,
                    final HttpServletResponse resp) throws IOException {
        String cmdText = Streams.readAndClose(req.getReader());
        cmdText = URLDecoder.decode(cmdText, "UTF-8");

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 准备输出
        HttpRespStatusSetter _resp = new HttpRespStatusSetter(resp);
        OutputStream out = new AppRespOutputStreamWrapper(_resp, 200);
        OutputStream err = new AppRespOutputStreamWrapper(_resp, 500);
        final Writer w = new OutputStreamWriter(out);

        // 运行
        WnSession se = Wn.WC().checkSE();
        _run_cmd("", se, cmdText, out, err, null, new Callback<WnBoxContext>() {
            public void invoke(WnBoxContext bc) {
                WnSession se = bc.sessionService.check(bc.session.id());
                if (!Strings.isBlank(metaOutputSeparator))
                    try {
                        w.write("\n" + metaOutputSeparator + ":BEGIN:envs\n");
                        w.write(Json.toJson(se.envs()));
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
