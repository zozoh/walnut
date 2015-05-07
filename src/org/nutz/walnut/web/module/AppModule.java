package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.View;
import org.nutz.mvc.annotation.*;
import org.nutz.walnut.api.box.WnBox;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.Jvms;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.web.bean.WnApp;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/a")
@Filters(@By(type = WnCheckSession.class))
public class AppModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    @Inject("java:$conf.getInt('box-alloc-timeout')")
    private int allocTimeout;

    @At("/open/?/**")
    @Ok("jsp:jsp.app")
    @Fail("jsp:jsp.show_text")
    public CharSequence open(String appName, String str) throws UnsupportedEncodingException {
        WnObj oAppHome = this._check_app_home(appName);

        // 要处理的对象
        WnObj o = Strings.isBlank(str) ? null : Wn.checkObj(io, str);

        // 生成 app 的对象
        WnApp app = new WnApp();
        app.setObj(o);
        app.setSession(Wn.WC().checkSE());
        app.setName(appName);

        String appJson = Json.toJson(app, JsonFormat.forLook().setQuoteName(true));

        // 找到主界面模板
        String tt = "pc"; // 可以是 "pc" 或者 "mobile"

        WnObj oTmpl = io.fetch(oAppHome, tt + "_tmpl.html");
        if (null == oTmpl) {
            oTmpl = io.check(null, "/etc/app/dft_app_" + tt + "_tmpl.html");
        }

        // 读取模板并分析
        String tmpl = io.readText(oTmpl);
        Segment seg = Segments.create(tmpl);

        // 标题
        String title = appName;
        if (null != o)
            title = o.name() + " : " + title;

        // 填充模板占位符
        seg.set("title", title);
        seg.set("rs", conf.get("app-rs"));
        seg.set("appName", appName);
        seg.set("app", appJson);

        // 渲染输出
        return seg.render();
    }

    @At("/load/?/**")
    @Ok("void")
    @Fail("http:404")
    public View load(String appName, String rsName) {
        WnObj oAppHome = this._check_app_home(appName);
        WnObj o = io.check(oAppHome, rsName);
        return new WnObjDownloadView(io, o);
    }

    @At("/run/?/*")
    @Ok("void")
    public void run(String appName,
                    String mimeType,
                    HttpServletRequest req,
                    HttpServletResponse resp) throws IOException {

        // 得到命令行
        String cmdText = Strings.trim(URLDecoder.decode(req.getQueryString(), "UTF-8"));
        String[] cmdLines = Jvms.split(cmdText, true, '\n', ';');

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 得到一个沙箱
        WnBox box = boxes.alloc(allocTimeout);

        if (log.isDebugEnabled())
            log.debugf("box:alloc: %s", box.id());

        // 保存到请求属性中，box.onClose 的时候会删除这个属性
        req.setAttribute(WnBox.class.getName(), box);

        // 设置沙箱
        WnContext wc = Wn.WC();
        WnBoxContext bc = new WnBoxContext();
        bc.io = io;
        bc.me = usrs.check(wc.checkMe());
        bc.session = wc.checkSE();
        bc.usrService = usrs;
        bc.sessionService = sess;

        if (log.isDebugEnabled())
            log.debugf("box:setup: %s", bc);
        box.setup(bc);

        // 准备回调
        if (log.isDebugEnabled())
            log.debug("box:set stdin/out/err");

        OutputStream out = new AppRespOutputStreamWrapper(resp, 200);
        OutputStream err = new AppRespOutputStreamWrapper(resp, 500);

        box.setStdin(null); // HTTP GET 方式，不支持沙箱的 stdin
        box.setStdout(out);
        box.setStderr(err);

        // 运行
        if (log.isDebugEnabled())
            log.debugf("box:run: %s", cmdText);
        for (String cmdLine : cmdLines) {
            box.submit(cmdLine);
        }
        box.run();

        // 释放沙箱
        if (log.isDebugEnabled())
            log.debugf("box:free: %s", box.id());
        boxes.free(box);

        if (log.isDebugEnabled())
            log.debug("box:done");
    }

}
