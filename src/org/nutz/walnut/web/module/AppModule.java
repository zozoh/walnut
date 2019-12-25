package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import org.nutz.lang.util.NutMap;
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
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.bean.WnApp;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.impl.WnAppService;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnAddCookieViewWrapper;
import org.nutz.walnut.web.view.WnObjDownloadView;
import org.nutz.web.ajax.AjaxView;

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

        try {
            // 得到应用
            WnApp app = apps.checkApp(appName);

            // 得到数据对象
            if (Strings.isBlank(str)) {
                str = app.getSession().getVars().getString("OBJ_DFT_PATH", "~");
            }
            WnObj obj = apps.getObj(app, str);
            app.setObj(obj);

            // 渲染模板
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
        WnApp app = apps.checkApp(appName);

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 准备输出
        HttpRespStatusSetter _resp = new HttpRespStatusSetter(resp);
        OutputStream out = new AppRespOutputStreamWrapper(_resp, 200);
        OutputStream err = new AppRespOutputStreamWrapper(_resp, 500);
        InputStream ins = Strings.isEmpty(in) ? null : Lang.ins(in);

        // 执行
        apps.runCommand(app, metaOutputSeparator, PWD, cmdText, out, err, ins);
    }

    /**
     * @param ticket
     * @return
     */
    @At
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public View auth_by_domain(@Param("site") String siteId,
                               @Param("ticket") String ticket,
                               @Param("ajax") boolean ajax) {
        // -------------------------------
        // 站点/票据/服务类
        WnObj oWWW = io.checkById(siteId);
        WnWebService webs = new WnWebService(io, oWWW);

        // 特殊会话类型
        String byType = "auth_by_domain";
        String byValue = siteId + ":" + ticket;

        // -------------------------------
        // 查找之前的会话
        WnAuthSession seSys = auth.getSession(byType, byValue);

        // -------------------------------
        // 嗯，看来要自动创建一个新的咯
        // -------------------------------
        if (null == seSys) {
            // 得到站点的会话票据
            WnAuthSession seDmn = webs.getAuthApi().checkSession(ticket);

            // 得到用户
            WnAccount u = seDmn.getMe();

            // 注册新会话
            seSys = auth.createSession(u, true);

            // 标注新会话的类型，以便指明用户来源
            seSys.setByType(byType);
            seSys.setByValue(byValue);

            // 确保用户会话有足够的环境变量
            NutMap vars = seSys.getVars();
            vars.putDefault("PATH", "/bin:/sbin:~/bin");
            vars.putDefault("APP_PATH", "/rs/ti/app:/app");
            vars.putDefault("VIEW_PATH", "/rs/ti/view/");
            vars.putDefault("SIDEBAR_PATH", "~/.ti/sidebar.json:/rs/ti/view/sidebar.json");
            vars.putDefault("OPEN", "wn.manager");

            // 保存会话
            auth.saveSession(seSys);
        }

        // 准备返回数据
        NutMap se = seSys.toMapForClient();

        View view;
        // 返回AJAX 视图
        if (ajax) {
            view = new WnAddCookieViewWrapper(new AjaxView(), null);
        }
        // 重定向视图
        else {
            view = new WnAddCookieViewWrapper("/");
        }

        // 包裹数据对象并返回
        return new ViewWrapper(view, se);
    }

}
