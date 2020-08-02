package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

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
import org.nutz.mvc.annotation.Attr;
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
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.impl.srv.WnDomainService;
import org.nutz.walnut.impl.srv.WwwSiteInfo;
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
            WnObj o = io().check(oAppHome, rsName);

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
            return new WnObjDownloadView(io(), o, ua, etag, range);
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
     * 采用域站点账户模型登录
     * 
     * @param siteId
     *            站点的 ID
     * @param name
     *            用户名
     * @param passwd
     *            密码
     * @param ajax
     *            返回的会话是否用 Ajax 形式包裹
     * @param hostName
     *            转接的域名
     * @return 输出视图
     */
    @At
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public View auth_login_by_domain_passwd(@Param("site") String siteId,
                                            @Param("name") String name,
                                            @Param("passwd") String passwd,
                                            @Param("ajax") boolean ajax,
                                            @Attr("wn_www_host") String hostName) {
        View view = null;
        Object reo = null;
        WnDomainService domains = new WnDomainService(io());
        WwwSiteInfo si = domains.getWwwSiteInfo(siteId, hostName);
        // -------------------------------------------------
        if (null == si.oWWW) {
            if (ajax) {
                view = new AjaxView();
            } else {
                view = new ServerRedirectView("/");
            }
            reo = Er.create("e.auth.login.domain_without_www");
            // 包裹返回
            return new ViewWrapper(view, reo);
        }
        // -------------------------------------------------
        // 如果这个域声明了默认登录站点，那么则试图用这个站点的账户系统登录
        try {
            // 如果采用域用户登陆，则校验系统账户
            // 并返回 CookieView
            if (si.oHome.isSameName(name)) {
                reo = auth().loginByPasswd(name, passwd);
            }
            // 采用域用户库来登陆
            else {
                WnAccount user = si.webs.getAuthApi().checkAccount(name);
                // -----------------------------------------
                // 检查登录密码，看看是否登录成功
                if (user.isMatchedRawPasswd(passwd)) {
                    // 确保用户是可以访问域主目录的
                    __check_home_accessable(si.oHome, user);

                    // 特殊会话类型
                    String byType = "auth_by_domain";
                    String byValue = si.siteId + ":passwd";

                    // 注册新会话
                    WnAuthSession se = auth().createSession(user, true);

                    // 更新会话元数据
                    __update_auth_session(se, si.webs, byType, byValue);

                    // 准备返回值
                    reo = se;
                }
                // -----------------------------------------
                // 登录失败
                else {
                    reo = Er.create("e.auth.login.invalid.passwd");
                }
            }

            // 根据选项包裹返回视图
            // AJAX 视图
            if (ajax) {
                view = new WnAddCookieViewWrapper(new AjaxView(), null);
            }
            // 重定向视图
            else {
                view = new WnAddCookieViewWrapper("/");
            }
            // 返回
            return new ViewWrapper(view, reo);
        }
        // 通常是账户不存在或者权限错误，进入这个分支
        catch (Exception e) {
            reo = e;
        }
        // -----------------------------------------
        // 进行到这里一定出现了错误，这里准备一下错误视图
        if (ajax) {
            view = new AjaxView();
        } else {
            view = new ServerRedirectView("/");
        }
        // -----------------------------------------
        // 包裹返回
        return new ViewWrapper(view, reo);
    }

    /**
     * 根据域站点登录票据进行系统会话登录
     * 
     * @param siteId
     *            站点的 ID
     * @param ticket
     *            用户登录票据
     * @param ajax
     *            是否要返回 ajax 形式的包裹
     * @param hostName
     *            转接的域名
     * @return 输出视图
     */
    @At
    @Filters(@By(type = WnAsUsr.class, args = {"root"}))
    public View auth_login_by_domain_ticket(@Param("site") String siteId,
                                            @Param("ticket") String ticket,
                                            @Param("ajax") boolean ajax,
                                            @Attr("wn_www_host") String hostName) {
        View view;
        Object reo;
        WnDomainService domains = new WnDomainService(io());
        WwwSiteInfo si = domains.getWwwSiteInfo(siteId, hostName);
        // -------------------------------------------------
        if (null == si.oWWW) {
            if (ajax) {
                view = new AjaxView();
            } else {
                view = new ServerRedirectView("/");
            }
            reo = Er.create("e.auth.login.domain_without_www");
            // 包裹返回
            return new ViewWrapper(view, reo);
        }
        try {
            // -------------------------------
            // 特殊会话类型
            String byType = "auth_by_domain";
            String byValue = si.siteId + ":" + ticket;

            // -------------------------------
            // 查找之前的会话
            WnAuthSession seSys = auth().getSession(byType, byValue);

            // -------------------------------
            // 嗯，看来要自动创建一个新的咯
            // -------------------------------
            if (null == seSys || seSys.isDead()) {
                // 得到站点的会话票据
                WnAuthSession seDmn = si.webs.getAuthApi().checkSession(ticket);

                // 得到用户
                WnAccount u = seDmn.getMe();

                // 确保用户是可以访问域主目录的
                __check_home_accessable(si.oHome, u);

                // 注册新会话
                seSys = auth().createSession(u, true);

                // 更新会话元数据
                __update_auth_session(seSys, si.webs, byType, byValue);
            }

            // 准备返回数据
            NutMap se = seSys.toMapForClient();

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
        // 通常是存在什么问题，则会进入这个分支
        catch (Exception e) {
            reo = e;
        }
        // -----------------------------------------
        // 进行到这里一定出现了错误，这里准备一下错误视图
        if (ajax) {
            view = new AjaxView();
        } else {
            view = new ServerRedirectView("/");
        }
        // -----------------------------------------
        // 包裹返回
        return new ViewWrapper(view, reo);
    }

    private void __check_home_accessable(WnObj oHome, WnAccount user) {
        WnSecurity secu = new WnSecurityImpl(io(), auth());
        // 不能读，那么注销会话，并返回错误
        if (!secu.test(oHome, Wn.Io.R, user)) {
            throw Er.create("e.auth.home.forbidden");
        }
    }

    private void __update_auth_session(WnAuthSession se,
                                       WnWebService webs,
                                       String byType,
                                       String byValue) {
        // 标注新会话的类型，以便指明用户来源
        se.setByType(byType);
        se.setByValue(byValue);

        // 确保用户会话有足够的环境变量
        NutMap vars = se.getVars();

        // 先搞一轮站点的环境变量，这个要强制加上
        for (Map.Entry<String, Object> en : webs.getSite().getVars().entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            boolean force = key.startsWith("!");
            // 有些时候，站点这边希望强制用户设置某些环境变量，譬如 HOME,THEME等
            // 这样就不用为每个用户设置了。有些时候又希望千人千面。
            // 所以我们把决定权交给配置，前面声明了 ! 的键，表示要强制设置的项目
            if (force) {
                key = key.substring(1).trim();
                vars.put(key, val);
            }
            // 弱弱的补充一下
            else {
                vars.putDefault(key, val);
            }
        }
        // 再搞一轮系统的默认环境变量，系统的，自然就都是弱弱的补充了，嗯，我看没什么问题
        for (Map.Entry<String, Object> en : conf.getInitUsrEnvs().entrySet()) {
            vars.putDefault(en.getKey(), en.getValue());
        }

        // 保存会话
        auth().saveSession(se);
    }
}
