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
import org.nutz.lang.Encoding;
import org.nutz.lang.Strings;
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

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.lookup.WnLookup;
import com.site0.walnut.lookup.impl.WnLookupMaker;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnContext;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.bean.WnLoginPage;
import com.site0.walnut.web.filter.WnAsUsr;
import com.site0.walnut.web.filter.WnCheckSession;
import com.site0.walnut.web.impl.WnAppService;
import com.site0.walnut.web.module.app.WnAppModuleCaptcha;
import com.site0.walnut.web.module.app.WnAppModuleDomainLoginByPassword;
import com.site0.walnut.web.module.app.WnAppModuleLoad;
import com.site0.walnut.web.module.app.WnAppModuleOpen;
import com.site0.walnut.web.module.app.WnAppModuleOTPSend;
import com.site0.walnut.web.module.app.WnAppModuleRun;
import com.site0.walnut.web.module.app.WnAppModuleSysLoginByPassword;
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
    @At("/open/?/**")
    @Fail("jsp:jsp.show_text")
    public View open(String appName,
                     @Param("ph") String str,
                     @Param("id") String id,
                     @ReqHeader("If-None-Match") String etag,
                     HttpServletResponse resp) {
        WnAppModuleOpen service = new WnAppModuleOpen();
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
        WnAppModuleLoad service = new WnAppModuleLoad();
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
                                             Encoding.CHARSET_UTF8);
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
        WnAppModuleRun service = new WnAppModuleRun();
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
        WnAppModuleSysLoginByPassword service = new WnAppModuleSysLoginByPassword();
        service.setRun(this);
        service.setIo(io());
        service.setAuth(auth());
        service.setConf(conf);
        service.setName(name);
        service.setPasswd(passwd);
        service.setOtpConfigName(otpConfigName);
        service.setOtpCode(otpCode);
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
                Object reo = pse.toBean(auth);
                return new ViewWrapper(new WnAddCookieViewWrapper(conf,
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
        WnAppModuleDomainLoginByPassword service = new WnAppModuleDomainLoginByPassword();
        service.setRun(this);
        service.setIo(io());
        service.setAuth(auth());
        service.setConf(conf);
        service.setSiteId(siteId);
        service.setAccount(name);
        service.setPasswd(passwd);
        service.setOtpConfigName(otpConfigName);
        service.setOtpCode(otpCode);
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
     * 验证码按域配置：路径参数 <code>domain</code> 是域（系统用户名）， 配置位于其家目录的
     * <code>~/.captcha/{captcha}.json</code>（配置名 缺省
     * <code>default</code>）；不带路径参数时表示 root 用户。 返回统一用 <code>AjaxReturn</code>
     * 包裹：成功 <code>{ok:true, data: WnCaptchaObj 的 JSON}</code>（含 <code>id</code>
     * 与渲染数据），客户端拿到即可绘制，零额外请求；失败 <code>{ok:false, errCode, msg}</code>。
     *
     * @param domain
     *            域（系统用户名），空表示 root 用户
     * @param captchaName
     *            验证码配置名
     * @return 验证码 JSON 视图
     */
    @At({"/captcha/get", "/captcha/get/?"})
    @Ok("ajax")
    @Fail("ajax")
    public View captcha_get(String domain,
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
        WnAppModuleCaptcha service = new WnAppModuleCaptcha();
        service.setAuth(auth());
        service.setIo(io());
        service.setDomain(domain);
        service.setCaptchaName(captchaName);
        service.setReq(req);
        service.setResp(resp);
        return service.get();
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
                                         Encoding.CHARSET_UTF8);
        String json = Streams.readAndClose(r);
        NutMap map = Json.fromJson(NutMap.class, json);
        // 防守一下，有时候会传入空
        if (null == map) {
            map = new NutMap();
        }
        // 请求体里的域必须和路径参数一致：要么都为空，要么完全相等
        String inputDomain = map.getString("domain");
        if (!Strings.equals(inputDomain, domain)) {
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
        WnAppModuleCaptcha service = new WnAppModuleCaptcha();
        service.setAuth(auth());
        service.setIo(io());
        service.setDomain(inputDomain);
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
     * 签发的临时票据（绑定账号、一次性、带过期），这里消费—— 校验不过直接拒绝，不触发任何发送。OTP 与 captcha 的配置都取自
     * 域（系统用户名）的家目录，不带路径参数时表示 root 用户。
     *
     * @param domain
     *            域（系统用户名），空表示 root 用户
     * @param otpConfigName
     *            OTP 配置名（对应 <code>~/.otp/{name}.json5</code>）
     * @param captchaName
     *            captcha 配置名（对应 <code>~/.captcha/{name}.json</code>）
     * @param ticket
     *            captcha verify 签发的临时票据
     * @param account
     *            接收 OTP 的账号（手机号或邮箱）
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
        WnAppModuleOTPSend service = new WnAppModuleOTPSend();
        service.setAuth(auth());
        service.setIo(io());
        service.setDomain(domain);
        service.setOtpConfigName(otpConfigName);
        service.setCaptchaName(captchaName);
        service.setTicket(ticket);
        service.setAccount(account);
        service.setReq(req);
        service.setResp(resp);
        return service.send();
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
