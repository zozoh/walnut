package com.site0.walnut.web.module;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;

import com.site0.walnut.util.Ws;
import org.nutz.lang.Streams;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
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
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;
import org.nutz.web.ajax.AjaxView;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.lookup.WnLookup;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.bean.WnLoginPage;
import com.site0.walnut.web.filter.WnAsUsr;
import com.site0.walnut.web.filter.WnCheckSession;
import com.site0.walnut.web.impl.WnAppService;
import com.site0.walnut.web.module.app.WamCaptcha;
import com.site0.walnut.web.module.app.WamDomainLoginByPassword;
import com.site0.walnut.web.module.app.WamLoad;
import com.site0.walnut.web.module.app.WamOcmInfo;
import com.site0.walnut.web.module.app.WamOpen;
import com.site0.walnut.web.module.app.WamOTPSend;
import com.site0.walnut.web.module.app.WamRun;
import com.site0.walnut.web.module.app.WamSysLoginByPassword;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnAddCookieViewWrapper;
import com.site0.walnut.web.view.WnDelCookieViewWrapper;

@IocBean
@At("/a")
public class AppModule extends AbstractWnModule {

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
            return login_page(null,
                              page,
                              domainName,
                              host,
                              sitePath,
                              etag,
                              range,
                              resp);
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
        login.setConfig(this.getConfig().toBean());
        login.setPageName(page);
        login.setDomainUser(domainUser);
        login.setHost(host);
        login.setSitePath(sitePath);
        login.setEtag(etag);
        login.setRange(range);
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
    @At("/open/?/**")
    @Fail("jsp:jsp.show_text")
    public View open(String appName,
                     @Param("ph") String str,
                     @Param("id") String id,
                     @ReqHeader("If-None-Match") String etag,
                     HttpServletResponse resp) {
        WamOpen service = new WamOpen();
        service.setApps(apps);
        service.setRun(this);
        service.setAppName(appName);
        service.setObjPath(str);
        service.setObjId(id);
        service.setEtag(etag);
        service.setResp(resp);
        return service.invoke();
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
        // 查找 app 的主目录
        WnObj oAppHome = this._check_app_home(appName);

        // 读取资源对象并输出
        WamLoad service = new WamLoad();
        service.setIo(io());
        service.setAppName(appName);
        service.setAppHome(oAppHome);
        service.setRsName(rsName);
        service.setMimeType(mimeType);
        service.setDownload(download);
        service.setUa(ua);
        service.setEtag(etag);
        service.setRange(range);
        service.setReq(req);
        service.setResp(resp);
        return service.invoke();
    }

    /**
     * 如果想在浏览器 unload 的时候执行一个命令，只能通过 <code>navigator.sendBeacon()</code>
     * 发送请求才能确保服务器会收到，但是不幸的时候，它只能发送 POST 请求，且不能指定 HEADER
     * <p>
     * 我们的 <code>run</code> 接口，假设浏览器发送的是
     * <code>Content-Type: application/x-www-form-urlencoded;</code> 但是
     * <code>navigator.sendBeacon()</code> 只能根据内容，自动决定 <code>ContentType</code>
     * 有时候（譬如我刚才）将 body 直接变成<code>x-www-form-urlencoded</code>的字符串发送过来，但是
     * HttpServletRequest 获取 body 参数的时候就不会解析 body ，因此也拿不到参数，这坑了我好几个小时。
     *
     * 为了能可靠，我建立这样一个接口，无论你 POST 过来的是什么 <code>ContentType</code> 我都会把请求内容 按照 JSON
     * 解析，并且得到 run 函数需要的参数
     *
     * @param appName
     * @param req
     * @param resp
     * @throws IOException
     */
    @Filters(@By(type = WnCheckSession.class, args = {"true"}))
    @At("/beacon_run/**")
    @Ok("void")
    @Fail("ajax")
    public void beacon_run(String appName,
                           final HttpServletRequest req,
                           final HttpServletResponse resp)
            throws IOException {
        try {
            Reader r = new InputStreamReader(req.getInputStream(),
                                             java.nio.charset.StandardCharsets.UTF_8);
            String json = Streams.readAndClose(r);
            NutMap map = Json.fromJson(NutMap.class, json);
            // 防守一下，有时候会传入空
            if (null == map) {
                return;
            }
            String mime = map.getString("mime");
            String mos = map.getString("mos");
            String PWD = map.getString("PWD");
            String cmd = map.getString("cmd");
            String in = map.getString("in");
            boolean ffb = map.getBoolean("ffb");
            boolean log_off = map.getBoolean("log_off");
            run(appName, mime, mos, PWD, cmd, in, ffb, log_off, req, resp);
        }
        catch (Exception err) {}
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
                    @Param("log_off") boolean set_log_off,
                    final HttpServletRequest req,
                    final HttpServletResponse resp)
            throws IOException {
        WamRun service = new WamRun();
        service.setApps(apps);
        service.setAuth(auth());
        service.setAppName(appName);
        service.setMimeType(mimeType);
        service.setMetaOutputSeparator(metaOutputSeparator);
        service.setPWD(PWD);
        service.setCmdText(cmdText);
        service.setIn(in);
        service.setForceFlushBuffer(forceFlushBuffer);
        service.setLogOff(set_log_off);
        service.setReq(req);
        service.setResp(resp);
        service.invoke();
    }

    /**
     * 登录系统会话
     *
     * <p>
     * 操作上下文（<code>WamOcmInfo</code>）由 {@link #get_ocm_info(String, String)}
     * 按登录模式构建（系统权鉴登录，配置主目录即 root 家目录，规则见
     * <code>62-auth-with-captcha-otp.md</code>）
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
                                    @Param("otp") String otpConfigName,
                                    @Param("otp_val") String otpCode,
                                    @Param("ajax") boolean ajax,
                                    @ReqHeader("Referer") String referer,
                                    final HttpServletRequest req,
                                    final HttpServletResponse resp) {
        WamOcmInfo ocm = this.get_ocm_info(null, name);
        WamSysLoginByPassword service = new WamSysLoginByPassword();
        service.setRun(this);
        service.setIo(io());
        service.setSysAuth(auth());
        service.setConf(getConfig());
        service.setName(name);
        service.setPasswd(passwd);
        service.setOtpConfigName(otpConfigName);
        service.setOtpCode(otpCode);
        service.setOcm(ocm);
        service.setAjax(ajax);
        service.setReferer(referer);
        service.setReq(req);
        service.setResp(resp);
        return service.invoke();
    }

    private View __get_session_default_view(WnSession se) {
        String appName = se.getEnv().getString("OPEN", "wn-term");
        String url = "/a/open/" + appName;
        Object reData = se.toBean(auth());
        return new ViewWrapper(new WnAddCookieViewWrapper(getConfig(), url), reData);
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
        String entry = getConfig().getSysEntryUrl();
        View view = ajax ? new AjaxView() : new ServerRedirectView(entry);
        WnContext wc = Wn.WC();
        if (wc.hasTicket()) {
            String ticket = wc.getTicket();
            // 退出登录
            WnLoginApi auth = this.auth();
            WnSession pse = auth.logout(ticket);

            // 退到父会话
            if (null != pse && !pse.isExpired()) {
                Object reo = pse.toBean(auth);
                return new ViewWrapper(new WnAddCookieViewWrapper(getConfig(),
                                                                  view,
                                                                  null),
                                       reo);
            }
        }

        // 直接删除会话收工
        return new WnDelCookieViewWrapper(view);
    }

    /**
     * 采用域站点账户模型登录
     *
     * <p>
     * 操作上下文（<code>WamOcmInfo</code>）由 {@link #get_ocm_info(String, String)}
     * 按登录模式构建：先解析站点拿到 domain，配置主目录规则见 <code>62-auth-with-captcha-otp.md</code>
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
                                            @Param("otp") String otpConfigName,
                                            @Param("otp_val") String otpCode,
                                            @Param("ajax") boolean ajax,
                                            @Attr("wn_www_host") String hostName,
                                            final HttpServletRequest req,
                                            final HttpServletResponse resp) {
        // 先解析站点拿到 domain，以便按登录模式构建操作上下文
        // 站点只解析这一次，服务类复用本实例
        WnLoginSite site = WnLoginSite.create(io(), siteId, hostName);
        String domain = null == site ? null : site.getDomain();
        WamOcmInfo ocm = this.get_ocm_info(domain, name);
        WamDomainLoginByPassword service = new WamDomainLoginByPassword();
        service.setRun(this);
        service.setIo(io());
        service.setSysAuth(auth());
        service.setConf(getConfig());
        service.setSite(site);
        service.setSiteId(siteId);
        service.setAccount(name);
        service.setPasswd(passwd);
        service.setOtpConfigName(otpConfigName);
        service.setOtpCode(otpCode);
        service.setOcm(ocm);
        service.setAjax(ajax);
        service.setHostName(hostName);
        service.setReq(req);
        service.setResp(resp);
        return service.invoke();
    }

    /**
     * 生成一份人机验证码（captcha）
     *
     * <p>
     * 配置目录与登录模式由 {@link #get_ocm_info(String, String)} 决定 （规则见
     * <code>62-auth-with-captcha-otp.md</code>）：验证码配置位于
     * <code>{confHome}/.captcha/{captcha}.json</code>（配置名缺省
     * <code>default</code>）。返回统一用 <code>AjaxReturn</code> 包裹： 成功
     * <code>{ok:true, data: WnCaptchaObj 的 JSON}</code>（含 <code>id</code>
     * 与渲染数据），客户端拿到即可绘制，零额外请求； 失败 <code>{ok:false, errCode, msg}</code>。
     *
     * @param domain
     *            域（系统用户名），空表示系统权鉴
     * @param account
     *            登录名（可空：用户还没输入时，按域权鉴落域配置目录）
     * @param captchaName
     *            验证码配置名
     * @return 验证码 JSON 视图
     */
    @At({"/captcha/get", "/captcha/get/?"})
    @Ok("ajax")
    @Fail("ajax")
    public View captcha_get(String domain,
                            @Param("account") String account,
                            @Param("captcha") String captchaName,
                            final HttpServletRequest req,
                            final HttpServletResponse resp) {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        WamOcmInfo ocm = this.get_ocm_info(domain, account);
        WamCaptcha service = new WamCaptcha();
        service.setIo(io());
        service.setOcm(ocm);
        service.setCaptchaName(captchaName);
        service.setReq(req);
        service.setResp(resp);
        return service.get();
    }

    /**
     * 按登录模式解析出 captcha / OTP 服务所需的全部信息（规则见
     * <code>62-auth-with-captcha-otp.md</code>）：
     *
     * <ul>
     * <li><b>配置主目录</b>：系统权鉴登录（<code>domain</code> 为空或
     * <code>account == domain</code>）取 root 用户家目录；域权鉴登录取域 用户家目录</li>
     * <li><b>操作用户</b>：按登录模式取登录接口（{@link #get_auth(String, String)}）解析
     * <code>account</code>。captcha 阶段用户可能还不存在 （譬如注册场景），解析不到时置
     * <code>null</code>，由调用方决定 要不要报错</li>
     * </ul>
     *
     * @param domain
     *            域（系统用户名），空表示系统权鉴登录
     * @param account
     *            登录名，可为空
     * @return 操作上下文信息
     */
    protected WamOcmInfo get_ocm_info(String domain, String account) {
        WamOcmInfo re = new WamOcmInfo();
        re.setDomain(domain);
        re.setAccount(account);
        // 配置主目录：系统权鉴 → root；域权鉴 → 域
        String confUsrName = Ws.isBlank(domain)
                             || domain.equals(account) ? "root" : domain;
        WnUser confUsr = this.auth().checkUser(confUsrName);
        re.setConfHomePath(confUsr.getHomePath());
        // 操作用户：解析不到（未注册等）置 null
        try {
            WnLoginApi dmnAuth = this.get_auth(domain, account);
            re.setUser(dmnAuth.checkUser(account));
        }
        catch (Exception e) {
            re.setUser(null);
        }
        return re;
    }

    /**
     * 校验人机验证码，通过则签发绑定账号的临时票据
     *
     * <p>
     * 票据是一次性的，且带过期时间；客户端在后续业务请求（如 OTP 发送 / 登录提交）里带上
     * <code>ticket + account</code>，由业务侧 <code>checkTicket</code> 消费。
     *
     * <p>
     * 提交的请求体是一个裸 JSON 对象： <code>{id, domain, account, captcha, data}</code>——
     * <code>data</code> 是行为采集结果（对象或 JSON 字符串均可）； 请求体里的 <code>domain</code>
     * 必须和路径参数一致： 要么都为空（root 用户），要么完全相等，否则报
     * <code>e.captcha.domain.mismatch</code>
     *
     * @param domain
     *            域（系统用户名），空表示 root 用户
     * @return 成功 <code>{"ok":true,"data":{"ticket":"..."}}</code>；失败
     *         <code>{"ok":false,"errCode":...}</code>
     */
    @At({"/captcha/verify", "/captcha/verify/?"})
    @Ok("ajax")
    @Fail("ajax")
    public View captcha_verify(String domain,
                               final HttpServletRequest req,
                               final HttpServletResponse resp)
            throws IOException {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        // 读取裸 JSON 请求体
        Reader r = new InputStreamReader(req.getInputStream(),
                                         java.nio.charset.StandardCharsets.UTF_8);
        String json = Streams.readAndClose(r);
        NutMap map = Json.fromJson(NutMap.class, json);
        // 防守一下，有时候会传入空
        if (null == map) {
            map = new NutMap();
        }
        // 请求体里的域必须和路径参数一致：要么都为空，要么完全相等
        String inputDomain = map.getString("domain");
        if (!Ws.equals(inputDomain, domain)) {
            AjaxReturn re = Ajax.fail().setErrCode("e.captcha.domain.mismatch");
            String reJson = Json.toJson(re, JsonFormat.compact());
            return new ViewWrapper(new RawView("json"), reJson);
        }
        // data 是行为采集结果，对象的话序列化回字符串
        Object data = map.get("data");
        String captchaData = null;
        if (null != data) {
            if (data instanceof CharSequence) {
                captchaData = data.toString();
            } else {
                captchaData = Json.toJson(data, JsonFormat.compact());
            }
        }
        WamOcmInfo ocm = this.get_ocm_info(inputDomain,
                                           map.getString("account"));
        WamCaptcha service = new WamCaptcha();
        service.setIo(io());
        service.setOcm(ocm);
        service.setCaptchaName(map.getString("captcha"));
        service.setCaptchaId(map.getString("id"));
        service.setCaptchaData(captchaData);
        service.setAccount(map.getString("account"));
        service.setReq(req);
        service.setResp(resp);
        return service.verify();
    }

    /**
     * 发送一份 OTP 动态密码到指定账号
     *
     * <p>
     * 发送前必须先通过人机校验：参数 <code>ticket</code> 是 captcha verify
     * 签发的临时票据（绑定账号、一次性、带过期），这里消费—— 校验不过直接拒绝，不触发任何发送。 操作上下文（用户、配置目录等）由
     * {@link #get_ocm_info(String, String)} 按登录模式解析（详见
     * <code>62-auth-with-captcha-otp.md</code>）。
     *
     * @param domain
     *            域（系统用户名），空表示系统权鉴登录
     * @param otpConfigName
     *            OTP 配置名（对应 <code>~/.otp/{name}.json5</code>）
     * @param captchaName
     *            captcha 配置名（对应 <code>~/.captcha/{name}.json</code>）
     * @param ticket
     *            captcha verify 签发的临时票据
     * @param account
     *            登录名（用户名 / 手机 / 邮箱），OTP 发到其邮箱
     * @return 成功 <code>{"ok":true}</code>；失败
     *         <code>{"ok":false,"errCode":...}</code>
     */
    @At({"/otp/send", "/otp/send/?"})
    @Ok("ajax")
    @Fail("ajax")
    public View otp_send(String domain,
                         @Param("otp") String otpConfigName,
                         @Param("captcha") String captchaName,
                         @Param("ticket") String ticket,
                         @Param("account") String account,
                         final HttpServletRequest req,
                         final HttpServletResponse resp) {
        WamOcmInfo ocm = this.get_ocm_info(domain, account);
        WamOTPSend service = new WamOTPSend();
        service.setIo(io());
        service.setOcm(ocm);
        service.setOtpConfigName(otpConfigName);
        service.setCaptchaName(captchaName);
        service.setTicket(ticket);
        service.setAccount(account);
        service.setReq(req);
        service.setResp(resp);
        return service.send();
    }

    /**
     * 按登录模式获取登录接口（详见 <code>62-auth-with-captcha-otp.md</code>）：
     * <ul>
     * <li><b>系统权鉴登录</b>：<code>domain</code> 为空，或 <code>account</code> 与
     * <code>domain</code> 同名（只有系统用户库才有与域同名的账号）—— 直接返回系统默认权鉴接口</li>
     * <li><b>域权鉴登录</b>：声明了 <code>domain</code> 且 <code>account</code> 与域不同名——用
     * <code>WnLoginSite</code> 模式构建域站点自己的用户库 登录接口（与域站点登录
     * <code>auth_login_by_domain_passwd</code> 同一套路）</li>
     * </ul>
     *
     * @param domain
     *            域（系统用户名），空表示系统权鉴登录
     * @param account
     *            登录账号
     * @return 登录接口
     */
    protected WnLoginApi get_auth(String domain, String account) {
        // 系统权鉴登录：域为空，或账号与域同名
        if (Ws.isBlank(domain) || domain.equals(account)) {
            return this.auth();
        }
        // 域的家目录即站点配置所在，用它定位站点
        WnUser u = this.auth().checkUser(domain);
        NutBean vars = Wlang.map("HOME", u.getHomePath());
        // 默认的，先用 ~/www/login 来默认做登录站点
        String sitePath = Wn.normalizeFullPath("~/www/login", vars);
        WnLoginSite site = WnLoginSite.create(this.io(), sitePath, null);
        if (null == site) {
            throw Er.create("e.auth.login.domain_without_www", domain);
        }
        NutBean env = site.getSessionVarsBySiteHome();
        WnLoginOptions options = site.getOptions();
        return WnLoginApiMaker.forHydrate().make(this.io(), env, options);
    }

    @At
    @Ok("ajax")
    @Fail("ajax")
    public NutMap login_mode(final HttpServletResponse resp) {
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });
        NutMap re = new NutMap();
        re.put("system", getConfig().getSysLoginMode());
        re.put("domain", getConfig().getDomainLoginMode());

        return re;
    }

    @At("/me")
    @Ok("ajax")
    @Fail("ajax")
    public NutMap getMe(final HttpServletRequest req,
                        final HttpServletResponse resp) {
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        WnSession se = Wn.WC().checkSession(auth());
        return se.toBean(auth());
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
    public NutMap sys_ajax_logout(final HttpServletRequest req,
                                  final HttpServletResponse resp) {
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
                re.put("parent", pse.toBean(auth()));
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
                                final HttpServletRequest req,
                                final HttpServletResponse resp) {
        WnWeb.setCrossDomainHeaders("*", (name, value) -> {
            resp.setHeader(WnWeb.niceHeaderName(name), value);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }

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
