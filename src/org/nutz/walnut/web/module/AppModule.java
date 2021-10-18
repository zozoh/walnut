package org.nutz.walnut.web.module;

import java.io.IOException;
import java.io.InputStream;
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
import org.nutz.walnut.api.auth.WnAuthService;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnSecurity;
import org.nutz.walnut.ext.data.www.impl.WnWebService;
import org.nutz.walnut.impl.io.WnSecurityImpl;
import org.nutz.walnut.impl.srv.WnBoxRunning;
import org.nutz.walnut.impl.srv.WnDomainService;
import org.nutz.walnut.impl.srv.WwwSiteInfo;
import org.nutz.walnut.util.Wlog;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnContext;
import org.nutz.walnut.web.bean.WnApp;
import org.nutz.walnut.web.bean.WnLoginPage;
import org.nutz.walnut.web.filter.WnAsUsr;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.impl.AppCheckAccess;
import org.nutz.walnut.web.impl.WnAppService;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnAddCookieViewWrapper;
import org.nutz.walnut.web.view.WnDelCookieViewWrapper;
import org.nutz.walnut.web.view.WnObjDownloadView;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxView;

import redis.clients.jedis.exceptions.JedisException;

@IocBean
@At("/a")
public class AppModule extends AbstractWnModule {

    private static final Log log = Wlog.getAPP();

    public static View V_304 = new HttpStatusView(304);

    @Inject
    protected WnAppService apps;

    @At("/login")
    public View login(HttpServletRequest req,
                      @Param("pg") String page,
                      @Attr("wn_www_grp") String domainName,
                      @Attr("wn_www_host") String host,
                      @ReqHeader("If-None-Match") String etag,
                      @ReqHeader("Range") String range,
                      HttpServletResponse resp) {
        String uri = req.getRequestURI();
        if (uri.endsWith("/")) {
            return login_page(null, page, domainName, host, etag, range, resp);
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
                           @ReqHeader("If-None-Match") String etag,
                           @ReqHeader("Range") String range,
                           HttpServletResponse resp) {

        // 看看是否已经登录了
        String ticket = Wn.WC().getTicket();
        WnAuthSession se = auth().getSession(ticket);
        if (null != se && !se.isDead()) {
            return __get_session_default_view(se);
        }

        // 已经得到域用户
        WnAccount domainUser = null;
        if (null != domainName) {
            domainUser = auth().getAccount(domainName);
        }

        // 渲染登陆页面
        WnLoginPage login = new WnLoginPage();
        login.setIo(io());
        login.setPageName(page);
        login.setDomainUser(domainUser);
        login.setHost(host);
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
                     @ReqHeader("If-None-Match") String etag,
                     HttpServletResponse resp) {

        try {
            // 得到应用
            WnApp app = apps.checkApp(appName);

            // 得到数据对象
            if (Strings.isBlank(str)) {
                str = app.getSession().getVars().getString("OBJ_DFT_PATH", "~");
            }
            WnObj obj = apps.getObj(app, str);
            app.setObj(obj);

            // 检查应用权限: root 组成员免查，可以打开任何 app
            WnAuthSession se = app.getSession();
            WnObj oAppHome = app.getHome();
            if (!this.auth().isMemberOfGroup(se.getMe(), "root")) {
                WnIo io = io();
                WnObj oCheckAccess = io.fetch(oAppHome, "check_access.json");
                if (null != oCheckAccess) {
                    AppCheckAccess ca = io.readJson(oCheckAccess, AppCheckAccess.class);
                    WnAuthService auth = auth();
                    WnBoxRunning run = this.createRunning(false);
                    if (!ca.doCheck(io, se, auth, run)) {
                        return new HttpStatusView(403);
                    }
                }
            }

            // 渲染模板
            String html = apps.renderAppHtml(app);
            String sha1 = Lang.sha1(html);
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
        AppRespOpsWrapper out = new AppRespOpsWrapper(_resp, 200);
        AppRespOpsWrapper err = new AppRespOpsWrapper(_resp, 500);
        InputStream ins = Strings.isEmpty(in) ? null : Lang.ins(in);

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
    public View sys_login_by_passwd(@Param("name") String name,
                                    @Param("passwd") String passwd,
                                    @Param("ajax") boolean ajax,
                                    @ReqHeader("Referer") String referer) {
        referer = Strings.sBlank(referer, "/");
        try {
            WnAuthSession se = auth().loginByPasswd(name, passwd);
            // 如果是 Ajax 视图
            if (ajax) {
                Object reo = se.toMapForClient();
                return new ViewWrapper(new WnAddCookieViewWrapper(new AjaxView()), reo);
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

    private View __get_session_default_view(WnAuthSession se) {
        String appName = se.getVars().getString("OPEN", "wn.console");
        String url = "/a/open/" + appName;
        return new ViewWrapper(new WnAddCookieViewWrapper(url), se);
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
            WnAuthSession pse = auth().logout(ticket, 0);

            // 退到父会话
            if (null != pse && !pse.isDead()) {
                Object reo = pse.toMapForClient();
                return new ViewWrapper(new WnAddCookieViewWrapper(view), reo);
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
                                            @Attr("wn_www_host") String hostName) {
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
            // 如果采用域用户登陆，则校验系统账户
            // 并返回 CookieView
            if (si.oHome.isSameName(name)) {
                if (log.isInfoEnabled()) {
                    log.infof("Login as domain-user: %s", name);
                }
                WnAuthSession se = auth().loginByPasswd(name, passwd);
                String appName = se.getVars().getString("OPEN", "wn.console");
                redirectPath = "/a/open/" + appName;
                reo = se;
            }
            // 采用域用户库来登陆
            else {
                if (log.isInfoEnabled()) {
                    log.infof("Login as sub-user: %s", name);
                }
                WnAccount user = si.webs.getAuthApi().checkAccount(name);
                // -----------------------------------------
                // 检查登录密码，看看是否登录成功
                if (user.isMatchedRawPasswd(passwd)) {
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check passwd");
                    }
                    // 确保用户是可以访问域主目录的
                    __check_home_accessable(si.oHome, user);
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check_home_accessable");
                    }

                    // 特殊会话类型
                    String byType = WnAuthSession.V_BT_AUTH_BY_DOMAIN;
                    String byValue = si.siteId + ":passwd";

                    // 获取账号对应的角色

                    // 注册新会话
                    WnAuthSession se = auth().createSession(user, true);

                    // 更新会话元数据
                    __update_auth_session(se, si.webs, si, byType, byValue);

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
                    reo = se;
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
                view = new WnAddCookieViewWrapper(new AjaxView(), null);
            }
            // 重定向视图
            else {
                view = new WnAddCookieViewWrapper(redirectPath);
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
            String byType = WnAuthSession.V_BT_AUTH_BY_DOMAIN;
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
                __update_auth_session(seSys, si.webs, si, byType, byValue);
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
                                       WwwSiteInfo si,
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

        // 最后，设置一下所属站点，以备之后的权限检查相关的逻辑读取
        vars.put(WnAuthSession.V_WWW_SITE_ID, si.oWWW.id());
        vars.put(WnAuthSession.V_ROLE, "@" + se.getMe().getRoleName());

        // 保存会话
        auth().saveSession(se);
    }
}
