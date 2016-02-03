package org.nutz.walnut.web.module;

import java.io.IOException;
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
import org.nutz.mvc.view.JspView;
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
@Filters(@By(type = WnCheckSession.class, args = {"true"}))
public class AppModule extends AbstractWnModule {

    @Inject("java:$conf.get('page-app', 'app')")
    private String page_app;

    @Filters(@By(type = WnCheckSession.class))
    @At("/open/**")
    @Fail("jsp:jsp.show_text")
    public View open(String appName, @Param("ph") String str, @Param("m") boolean meta)
            throws UnsupportedEncodingException {

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

        // 这个是 app 的 JSON 描述
        String appJson = Json.toJson(app, JsonFormat.forLook().setQuoteName(true));

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
        NutMap c = new NutMap();
        c.put("title", title);

        // 添加自定义的上下文
        if (null != map)
            c.putAll(map);

        // 这些优先级最高
        c.put("session", seMap);
        c.put("rs", conf.get("app-rs"));
        c.put("appName", appName);
        c.put("app", appJson);
        c.put("appClass", appName.replace('.', '_').toLowerCase());

        // 渲染输出
        return new ViewWrapper(new JspView("jsp." + page_app), Tmpl.exec(tmpl, c));
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

        // TODO 这个木用，应该删掉，先去掉界面上那坨 var xxx = 就好
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

    @At("/run/**")
    @Ok("void")
    public void run(String appName,
                    @Param("mime") String mimeType,
                    @Param("mos") final String metaOutputSeparator,
                    @Param("PWD") String PWD,
                    @Param("cmd") String cmdText,
                    HttpServletRequest req,
                    final HttpServletResponse resp) throws IOException {
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
        final Writer w = new OutputStreamWriter(out);

        // 运行
        WnSession se = Wn.WC().checkSE();
        se.var("PWD", PWD);
        se.var("APP_HOME", oAppHome.path());

        _run_cmd("", se, cmdText, out, err, null, new Callback<WnBoxContext>() {
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
