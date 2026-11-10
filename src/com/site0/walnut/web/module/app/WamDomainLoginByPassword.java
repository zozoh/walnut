package com.site0.walnut.web.module.app;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.log.WnLog;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.ajax.AjaxView;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.WnLoginApiMaker;
import com.site0.walnut.login.WnLoginOptions;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.site.WnLoginSite;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.util.WnAppModuleLoginMode;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnAddCookieViewWrapper;

/**
 * 对应入口函数 <code>AppModule.auth_login_by_domain_passwd</code> 的服务类
 *
 * <p>
 * 认证模式由 <code>web.properties</code> 的 <code>app-module-dmn-login</code> 决定（缺省
 * <code>PASSWD_ONLY</code>，与历史行为一致），语义见 {@link WamSysLoginByPassword} 的类注释。
 *
 * <p>
 * 操作上下文（<code>WamOcmInfo</code>）由入口函数经
 * <code>AppModule.get_ocm_info(domain, account)</code> 构建传入：OTP 配置在
 * <code>{confHome}/.otp/{name}.json5</code>，配置主目录（
 * <code>ocm.confHomePath</code>）按登录模式取用（规则见
 * <code>62-auth-with-captcha-otp.md</code>）：域用户本身登录（系统权鉴）取 root
 * 家目录，子用户取域主目录。校验账号取用户的邮箱（见 {@link AbstractWamAuth#_check_otp}）
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WamDomainLoginByPassword extends AbstractWamAuth {

    private static final WnLog log = Wlog.getAPP();

    /**
     * 站点的 ID
     */
    private String siteId;

    /**
     * 登录账号（用户名 / 手机 / 邮箱）
     */
    private String account;

    /**
     * 转接的域名
     */
    private String hostName;

    /**
     * 本次请求解析出的登录站点（入口函数解析后传入，只解析一次）
     */
    private WnLoginSite site;

    public View invoke() {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        // -------------------------------------------------
        // 获取登录站点（入口函数已解析），找不到站点，那么一定要抛错
        Object reo = null;
        View view = null;
        WnLoginSite site = this.site;
        if (null == site) {
            if (ajax) {
                view = new AjaxView();
            } else {
                view = new ServerRedirectView("/");
            }
            if (log.isWarnEnabled()) {
                log.warnf("e.auth.login.domain_without_www: %s @ %s",
                          siteId,
                          hostName);
            }
            reo = Er.create("e.auth.login.domain_without_www");
            // 包裹返回
            return new ViewWrapper(view, reo);
        }
        // -------------------------------------------------
        // 如果这个域声明了默认登录站点，那么则试图用这个站点的账户系统登录
        String redirectPath = "/";
        // 登录模式
        WnAppModuleLoginMode mode = conf.getDomainLoginMode();
        try {
            // 站点用户类型：域用户本身走系统用户库，子用户走域用户库
            boolean asDomainOwner = site.isDomainOwner(account);
            WnLoginApi auth;
            // 系统域账户
            if (asDomainOwner) {
                auth = sysAuth;
            }
            // 域内账户
            else {
                WnLoginApiMaker loginMaker = WnLoginApiMaker.forHydrate();
                NutBean sessionVars = site.getSessionVarsBySiteHome();
                WnLoginOptions siteOptions = site.getOptions();
                auth = loginMaker.make(io, sessionVars, siteOptions);
            }
            if (log.isInfoEnabled()) {
                log.infof("Login as %s: %s",
                          asDomainOwner ? "sys-usr" : "domain-user",
                          account);
            }
            // 登录用户（不存在直接报错）
            WnUser user = auth.checkUser(account);
            // 按登录模式认证：OTP 轻量优先，密码其次，都过了才建会话
            switch (mode) {
            case OTP_ONLY:
                this._assert_otp(user);
                break;
            case PASSWD_ONLY:
                this.__assert_passwd(user, asDomainOwner);
                break;
            case PASSWD_AND_OTP:
                this._assert_otp(user);
                this.__assert_passwd(user, asDomainOwner);
                break;
            case PASSWD_OR_OTP:
                // 先看看是不是用密码
                if (!Ws.isBlank(passwd)) {
                    this.__assert_passwd(user, asDomainOwner);
                }
                // 那么就尝试 OTP 咯
                else if (!Ws.isBlank(otpCode)) {
                    this._assert_otp(user);
                }
                // 两个都为空，报密码错（与历史行为一致）
                else {
                    throw Er.create(this.__passwd_err_code(asDomainOwner));
                }
                break;
            }
            // -----------------------------------------
            // 认证通过，注册新会话（尽量晚创建）
            int se_du = auth.getSessionDuration();
            String seType = asDomainOwner ? Wn.SET_AUTH_PASS : Wn.SET_LOGIN_APP;
            WnSession se = auth.createSession(user, seType, se_du);
            // 子用户：确保用户是可以访问域主目录的
            if (!asDomainOwner) {
                site.assertHomeAccessable(se);
            }
            if (log.isInfoEnabled()) {
                log.infof("OK: create session: %s : %s",
                          se.getTicket(),
                          se.getMyName());
            }
            // 获取重定向路径
            String dftApp = asDomainOwner ? "wn-term" : "wn.manager";
            String appName = se.getEnv().getString("OPEN", dftApp);
            redirectPath = "/a/open/" + appName;
            // 准备返回值
            reo = se.toBean(auth);
            // 尝试初始化会话
            this._try_run_profile(se);
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

    /**
     * 断言密码校验通过，不过直接抛错
     *
     * @param user
     *            登录用户
     * @param asDomainUser
     *            是否域用户本身登录（错误码与历史行为一致：域用户报
     *            <code>e.auth.login.Failed</code>，子用户报
     *            <code>e.auth.login.invalid.passwd</code>）
     */
    private void __assert_passwd(WnUser user, boolean asDomainUser) {
        if (user.isMatchedRawPasswd(passwd)) {
            return;
        }
        throw Er.create(this.__passwd_err_code(asDomainUser));
    }

    /**
     * @param asDomainUser
     *            是否域用户本身登录
     * @return 密码错误码（与历史行为一致：域用户报 <code>e.auth.login.Failed</code>， 子用户报
     *         <code>e.auth.login.invalid.passwd</code>）
     */
    private String __passwd_err_code(boolean asDomainUser) {
        return asDomainUser ? "e.auth.login.Failed"
                            : "e.auth.login.invalid.passwd";
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String name) {
        this.account = name;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public WnLoginSite getSite() {
        return site;
    }

    public void setSite(WnLoginSite site) {
        this.site = site;
    }

}
