package com.site0.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
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
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxView;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnQuery;
import com.site0.walnut.impl.srv.WnBoxRunning;
import com.site0.walnut.impl.srv.WnDomainService;
import com.site0.walnut.impl.srv.WwwSiteInfo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnRoleList;
import com.site0.walnut.login.WnSession;
import com.site0.walnut.login.WnUser;
import com.site0.walnut.lookup.WnLookup;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.bean.WnApp;
import com.site0.walnut.web.bean.WnLoginPage;
import com.site0.walnut.web.filter.WnAsUsr;
import com.site0.walnut.web.filter.WnCheckSession;
import com.site0.walnut.web.impl.AppCheckAccess;
import com.site0.walnut.web.impl.WnAppService;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnAddCookieViewWrapper;
import com.site0.walnut.web.view.WnDelCookieViewWrapper;
import com.site0.walnut.web.view.WnObjDownloadView;

import redis.clients.jedis.exceptions.JedisException;

@IocBean
@At("/a")
public class AppModule extends AbstractWnModule {

    private static final Log log = Wlog.getAPP();

    public static View V_304 = new HttpStatusView(304);

    @Inject
    protected WnAppService apps;

    @Inject
    protected WnLookupMaker lookupMaker;

    @At("/login")
    public View login(HttpServletRequest req,
                      @Param("pg") String page,
                      @Attr("wn_www_grp") String domainName,
                      @Attr("wn_www_host") String host,
                      @Attr("wn_www_site") String sitePath,
                      @ReqHeader("If-None-Match") String etag,
                      @ReqHeader("Range") String range,
                      HttpServletResponse resp) {
        String uri = req.getRequestURI();
        if (uri.endsWith("/")) {
            return login_page(null, page, domainName, host, sitePath, etag, range, resp);
        }
        return new ServerRedirectView("/a/login/");
    }

    /**
     * 打开系统登录界面
     * 
     * @param page
     *            指定登录页面
     * 
     * @param domainName
     *            （来自 "wn_www_grp"）本次请求映射的 domain
     * @param host
     *            （来自 "wn_www_host"）本次请求映射的 host
     * @param sitePath
     *            （来自 "wn_www_site"）本次请求映射的站点目录
     * @param etag
     *            登录页（资源）的 ETag
     * @param range
     *            登录页的（资源） RangeDownload
     * @param resp
     *            响应对象
     * 
     * @return 登录界面视图
     */
    @At("/login/**")
    public View login_page(String rph,
                           @Param("pg") String page,
                           @Attr("wn_www_grp") String domainName,
                           @Attr("wn_www_host") String host,
                           @Attr("wn_www_site") String sitePath,
                           @ReqHeader("If-None-Match") String etag,
                           @ReqHeader("Range") String range,
                           HttpServletResponse resp) {

        // 看看是否已经登录了
        String ticket = Wn.WC().getTicket();
        WnSession se = this.auth().getSession(ticket);
        if (null != se && !se.isExpired()) {
            return __get_session_default_view(se);
        }

        // 已经得到域用户
        WnUser domainUser = null;
        if (null != domainName) {
            domainUser = this.auth().getUser(domainName);
        }

        // 渲染登陆页面
        WnLoginPage login = new WnLoginPage();
        login.setIo(io());
        login.setPageName(page);
        login.setDomainUser(domainUser);
        login.setHost(host);
        login.setSitePath(sitePath);
        login.setEtag(etag);
        login.setRange(range);
        login.setResp(resp);
        return login.genView(rph);
    }

    /**
     * 打开一个应用
     * 
     * @param appName
     *            应用名
     * @param str
     *            主对象
     * @param etag
     *            应用主 HTML 的指纹，以便 403
     * @return 应用视图
     */
    @Filters(@By(type = WnCheckSession.class))
    @At("/open/**")
    @Fail("jsp:jsp.show_text")
    public View open(String appName,
                     @Param("ph") String str,
                     @Param("id") String id,
                     @Param("m") String matchJson,
                     @ReqHeader("If-None-Match") String etag,
                     HttpServletResponse resp) {

        try {
            // 得到应用
            WnApp app = apps.checkApp(appName);

            if (log.isDebugEnabled()) {
                String envJson = Json.toJson(app.getSession().getEnv(), JsonFormat.nice());
                log.debugf("APP<%s>:%s:%s:%s:%s", appName, str, id, matchJson, envJson);
            }

            // 得到数据对象
            WnObj oP;
            // 指定 ID
            if (!Ws.isBlank(id)) {
                oP = apps.getObjById(app, id);
            }
            // 默认用路径
            else {
                if (Strings.isBlank(str)) {
                    str = app.getSession().getEnv().getString("OBJ_DFT_PATH", "~");
                }
                oP = apps.getObjByPath(app, str);
            }
            // 指定查询条件
            WnObj obj;
            if (!Ws.isBlank(matchJson)) {
                WnQuery q = Wn.Q.map(matchJson);
                q.setvToList("pid", oP.id());
                obj = apps.getObjByQuery(app, q);
            } else {
                obj = oP;
            }
            app.setObj(obj);

            // 检查应用权限: root 组成员免查，可以打开任何 app
            WnLoginApi auth = this.auth();
            WnSession se = app.getSession();
            WnObj oAppHome = app.getHome();
            WnUser me = se.getUser();
            WnRoleList roles = auth.getRoles(me);
            if (!roles.isMemberOfRole("root")) {
                WnIo io = io();
                WnObj oCheckAccess = io.fetch(oAppHome, "check_access.json");
                if (null != oCheckAccess) {
                    AppCheckAccess ca = io.readJson(oCheckAccess, AppCheckAccess.class);
                    WnBoxRunning run = this.createRunning(false);
                    if (!ca.doCheck(io, se, auth, run)) {
                        return new HttpStatusView(403);
                    }
                }
            }

            // 渲染模板
            String html = apps.renderAppHtml(app);
            String sha1 = Wlang.sha1(html);
            if (etag != null && sha1.equals(etag)) {
                return V_304;
            }
            resp.setHeader("ETag", sha1);
            return new ViewWrapper(new RawView("html"), html);
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to open", e);
            }
            return HttpStatusView.HTTP_404;
        }
    }

    /**
     * 加载应用内资源
     * 
     * @param appName
     *            应用名
     * @param rsName
     *            资源路径
     * @param mimeType
     *            资源 MIME
     * @param download
     *            是否下载
     * @param ua
     *            下载的 UA
     * @param etag
     *            本地缓存指纹
     * @param range
     *            Range Download
     * @param req
     *            请求对象
     * @param resp
     *            象用对象
     * @return 资源视图
     * @throws IOException
     */
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
            return new WnObjDownloadView(io(), o, null, ua, etag, range);
        }
        // 最后打印总时长
        finally {
            if (log.isDebugEnabled()) {
                sw.stop();
                log.debugf("APPLoad(%s) : %s DONE %s", appName, rsName, sw);
            }
        }
    }

    /**
     * 运行一条 Walnut 指令
     * 
     * @param appName
     * @param mimeType
     * @param metaOutputSeparator
     * @param PWD
     * @param cmdText
     * @param in
     * @param forceFlushBuffer
     * @param req
     * @param resp
     * @throws IOException
     */
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
                    @Param("ffb") boolean forceFlushBuffer,
                    final HttpServletRequest req,
                    final HttpServletResponse resp)
            throws IOException {
        // String cmdText = Streams.readAndClose(req.getReader());
        // cmdText = URLDecoder.decode(cmdText, "UTF-8");

        // 这个接口开放给外部 app 调用
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });

        // Options 无视
        if (WnWeb.isRequestOptions(req)) {
            return;
        }

        // 找到 app 所在目录
        WnApp app = apps.checkApp(appName);

        // 默认返回的 mime-type 是文本
        if (Strings.isBlank(mimeType))
            mimeType = "text/plain";
        resp.setContentType(mimeType);

        // 准备输出
        HttpRespStatusSetter _resp = new HttpRespStatusSetter(resp);
        AppRespOpsWrapper out = new AppRespOpsWrapper(_resp, 200);
        AppRespOpsWrapper err = new AppRespOpsWrapper(_resp, 500);
        InputStream ins = Strings.isEmpty(in) ? null : Wlang.ins(in);

        // 强制触发响应刷新缓冲
        if (forceFlushBuffer) {
            // 告知 Nginx 代理，不要缓存
            // TODO 看起来木有用
            resp.setHeader("X-ACCEL-BUFFERING", "yes");
            out.setForceFlush(true);
            err.setForceFlush(true);
        }

        // 执行
        apps.runCommand(app, metaOutputSeparator, PWD, cmdText, out, err, ins);
    }

    /**
     * 登录系统会话
     * 
     * @param name
     *            用户名
     * @param passwd
     *            密码
     * @param ajax
     *            返回的会话是否用 Ajax 形式包裹
     * @param referer
     *            来源 URL
     * @return 输出视图
     */
    @At
    public View sys_login_by_passwd(HttpServletRequest req,
                                    @Param("name") String name,
                                    @Param("passwd") String passwd,
                                    @Param("ajax") boolean ajax,
                                    @ReqHeader("Referer") String referer) {
        referer = Strings.sBlank(referer, "/");
        try {
            WnLoginApi auth = this.auth();
            WnSession se = auth.loginByPassword(name, passwd);
            // 如果是 Ajax 视图
            if (ajax) {
                // 获取 cookie 模板
                Object reo = se.toBean();
                return new ViewWrapper(new WnAddCookieViewWrapper(conf, new AjaxView(), null), reo);
            }
            // 直接跳转到用户的主应用
            return __get_session_default_view(se);
        }
        // 出错了
        catch (WebException e) {
            Throwable eCause = e.getCause();
            if (null != eCause && (eCause instanceof JedisException)) {
                if (log.isWarnEnabled()) {
                    log.warn("Jedis error", eCause);
                }
            }
            Object reo = Ajax.fail().setErrCode(e.getKey()).setData(e.getReason());
            // 返回视图
            if (ajax) {
                return new ViewWrapper(new AjaxView(), reo);
            }
            // 返回到原先地址
            return new ServerRedirectView(referer);
        }
    }

    private View __get_session_default_view(WnSession se) {
        String appName = se.getEnv().getString("OPEN", "wn.console");
        String url = "/a/open/" + appName;
        Object reData = se.toBean();
        return new ViewWrapper(new WnAddCookieViewWrapper(conf, url), reData);
    }

    /**
     * 注销当前系统会话
     * 
     * @param ajax
     *            返回的会话是否用 Ajax 形式包裹
     * @return 输出视图
     */
    @At
    public View sys_logout(@Param("ajax") boolean ajax) {
        String entry = conf.getSysEntryUrl();
        View view = ajax ? new AjaxView() : new ServerRedirectView(entry);
        WnContext wc = Wn.WC();
        if (wc.hasTicket()) {
            String ticket = wc.getTicket();
            // 退出登录
            WnLoginApi auth = this.auth();
            WnSession pse = auth.logout(ticket);

            // 退到父会话
            if (null != pse && !pse.isExpired()) {
                Object reo = pse.toBean();
                return new ViewWrapper(new WnAddCookieViewWrapper(conf, view, null), reo);
            }
        }

        // 直接删除会话收工
        return new WnDelCookieViewWrapper(view);
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
                                            @Attr("wn_www_host") String hostName,
                                            final HttpServletRequest req,
                                            final HttpServletResponse resp) {
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        View view = null;
        Object reo = null;
        WnDomainService domains = new WnDomainService(io());
        WwwSiteInfo si = domains.getWwwSiteInfo(siteId, hostName);
        String redirectPath = "/";
        if (log.isInfoEnabled()) {
            log.infof("auth_login_by_domain_passwd: - siteId: %s\n - name: %s\n - ajax: %s\n - host: %s",
                      siteId,
                      name,
                      ajax,
                      hostName);
        }
        // 防守一波
        if (null == si) {
            if (log.isWarnEnabled()) {
                log.warnf("e.auth.login.NilSiteInfo: %s @ %s", siteId, hostName);
            }
            WebException err = Er.create("e.auth.login.NilSiteInfo");
            if (ajax) {
                return new ViewWrapper(new AjaxView(), err);
            }
            return new ServerRedirectView(redirectPath);
        }
        // -------------------------------------------------
        if (null == si.oWWW) {
            if (ajax) {
                view = new AjaxView();
            } else {
                view = new ServerRedirectView("/");
            }
            if (log.isWarnEnabled()) {
                log.warnf("e.auth.login.domain_without_www: %s @ %s", siteId, hostName);
            }
            reo = Er.create("e.auth.login.domain_without_www");
            // 包裹返回
            return new ViewWrapper(view, reo);
        }
        // -------------------------------------------------
        // 如果这个域声明了默认登录站点，那么则试图用这个站点的账户系统登录
        try {
            WnLoginApi auth = this.auth();
            // 如果采用域用户登陆，则校验系统账户
            // 并返回 CookieView
            if (si.oHome.isSameName(name)) {
                if (log.isInfoEnabled()) {
                    log.infof("Login as domain-user: %s", name);
                }
                WnSession se = auth.loginByPassword(name, passwd);
                String appName = se.getEnv().getString("OPEN", "wn.console");
                redirectPath = "/a/open/" + appName;
                reo = se.toBean();
            }
            // 采用域用户库来登陆
            else {
                if (log.isInfoEnabled()) {
                    log.infof("Login as sub-user: %s", name);
                }
                WnUser user = si.webs.getAuthApi().checkAccount(name);
                if (log.isInfoEnabled()) {
                    log.infof("sub user : %s", user.toString());
                }
                // -----------------------------------------
                // 检查登录密码，看看是否登录成功
                if (user.isMatchedRawPasswd(passwd)) {
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check passwd");
                    }
                    // 确保用户是可以访问域主目录的
                    Session.checkHomeAccessable(io(), login(), si.oHome, user);
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check_home_accessable");
                    }

                    // 特殊会话类型
                    String byType = WnSession.V_BT_AUTH_BY_DOMAIN;
                    String byValue = si.siteId + ":passwd";

                    // 获取会话时长设置
                    int se_du = si.webs.getSite().getSeDftDu();

                    // 注册新会话
                    WnSession se = login().createSession(user, se_du);

                    // 更新会话元数据
                    Session.updateAuthSession(login(),
                                              conf.getInitUsrEnvs(),
                                              se,
                                              si.webs,
                                              si.oWWW,
                                              byType,
                                              byValue);

                    if (log.isInfoEnabled()) {
                        log.infof("OK: create session: %s : %s", byType, byValue);
                    }

                    // 获取重定向路径
                    String appName = se.getVars().getString("OPEN", "wn.manager");
                    redirectPath = "/a/open/" + appName;

                    if (log.isInfoEnabled()) {
                        log.infof(">>: %s", redirectPath);
                    }

                    // 准备返回值
                    reo = se.toMapForClient();
                }
                // -----------------------------------------
                // 登录失败
                else {
                    if (log.isInfoEnabled()) {
                        log.infof("KO: passwd fail");
                    }
                    reo = Er.create("e.auth.login.invalid.passwd");
                }
            }

            // 根据选项包裹返回视图
            // AJAX 视图
            if (ajax) {
                view = new WnAddCookieViewWrapper(conf, new AjaxView(), null);
            }
            // 重定向视图
            else {
                view = new WnAddCookieViewWrapper(conf, redirectPath);
            }
            // 返回
            return new ViewWrapper(view, reo);
        }
        // 通常是账户不存在或者权限错误，进入这个分支
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn(e.toString(), e);
            }
            reo = e;
        }
        // -----------------------------------------
        // 进行到这里一定出现了错误，这里准备一下错误视图
        if (ajax) {
            view = new AjaxView();
        } else {
            view = new ServerRedirectView(redirectPath);
        }
        // -----------------------------------------
        // 包裹返回
        return new ViewWrapper(view, reo);
    }

    @At("/me")
    @Ok("ajax")
    @Fail("ajax")
    public NutMap getMe(final HttpServletRequest req, final HttpServletResponse resp) {
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        WnSession se = Wn.WC().checkSession(auth());
        return se.toBean();
    }

    /**
     * 注销当前系统会话
     * 
     * @param ajax
     *            返回的会话是否用 Ajax 形式包裹
     * @return 输出视图
     */
    @At
    @Ok("ajax")
    @Fail("ajax")
    public NutMap sys_ajax_logout(final HttpServletRequest req, final HttpServletResponse resp) {
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        WnContext wc = Wn.WC();
        NutMap re = new NutMap();
        if (wc.hasTicket()) {
            String ticket = wc.getTicket();
            re.put("ticket", ticket);
            // 退出登录
            WnSession pse = this.auth().logout(ticket);

            // 退到父会话
            if (null != pse && !pse.isExpired()) {
                re.put("parent", pse.toBean());
            }
        }

        return re;
    }

    @At("/lookup/**")
    @Ok("ajax")
    @Fail("ajax")
    public List<NutBean> lookup(String lookupName,
                                @Param("id") String lookupId,
                                @Param("hint") String lookupHint,
                                @Param(value = "limit", df = "30") int limit,
                                @Param("reset") boolean reset,
                                final HttpServletResponse resp) {
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });

        if (reset) {
            lookupMaker.clear();
        }

        WnLookup lookup = lookupMaker.getLookup(lookupName);

        if (!Ws.isBlank(lookupId)) {
            return lookup.fetch(lookupId);
        }
        if (!Ws.isBlank(lookupHint)) {
            return lookup.lookup(lookupHint, limit);
        }
        return new ArrayList<>();
    }

}
