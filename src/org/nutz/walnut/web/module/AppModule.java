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

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.StringInputStream;
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
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.box.WnBoxContext;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.bean.WalnutApp;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.impl.WnAppService;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnObjDownloadView;

@IocBean
@At("/a")
public class AppModule extends AbstractWnModule {

    private static final Log log = Logs.get();

    public static View V_304 = new HttpStatusView(304);

    @Inject
    protected WnAppService apps;

    @Filters(@By(type = WnCheckSession.class))
    @At("/open/**")
    @Fail("jsp:jsp.show_text")
    public View open(String appName,
                     @Param("ph") String str,
                     @Param("m") boolean meta,
                     @ReqHeader("If-None-Match") String etag)
            throws UnsupportedEncodingException {

        // 得到应用
        try {
            WalnutApp app = apps.checkApp(appName, str);
            String html = apps.renderAppHtml(app);
            String sha1 = Lang.sha1(html);
            if (etag != null && sha1.equals(etag)) {
                return V_304;
            }
            Mvcs.getResp().setHeader("ETag", sha1);
            return new ViewWrapper(new RawView("html"), html);
        }
        catch (Exception e) {
            return HttpStatusView.HTTP_404;
        }
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
