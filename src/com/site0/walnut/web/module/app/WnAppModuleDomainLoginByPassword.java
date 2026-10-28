package com.site0.walnut.web.module.app;

import org.nutz.lang.util.NutBean;
import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.WebException;
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
 * 认证模式由 <code>web.properties</code> 的 <code>app-module-dmn-login</code>
 * 决定（缺省 <code>PASSWD_ONLY</code>，与历史行为一致），语义见
 * {@link WnAppModuleSysLoginByPassword} 的类注释。
 *
 * <p>
 * OTP 配置在域主目录的 <code>~/.otp/{name}.json5</code>（域用户和子用户
 * 共用）；校验账号取用户的邮箱（见
 * {@link AbstractWnAppModuleAuth#_check_otp}）
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleDomainLoginByPassword extends AbstractWnAppModuleAuth {

    private static final Log log = Wlog.getAPP();

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

    public View invoke() {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        // 获取登录接口
        Object reo = null;
        WnLoginSite site = WnLoginSite.create(io, siteId, hostName);
        // -------------------------------------------------
        // 找不到站点，那么一定要抛错
        View view = null;
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
        WnSession se = null;
        String redirectPath = "/";
        // 登录模式
        WnAppModuleLoginMode mode = conf.getDomainLoginMode();
        try {
            // 如果采用域用户登陆，则校验系统账户
            // 并返回 CookieView
            if (site.isDomainUser(account)) {
                se = this.__login_as_domain_user(mode);
                String appName = se.getEnv().getString("OPEN", "wn-term");
                redirectPath = "/a/open/" + appName;
                reo = se.toBean(auth);
            }
            // 采用域用户库来登陆
            else {
                NutBean env = site.getSessionVarsBySiteHome();
                WnLoginOptions options = site.getOptions();
                WnLoginApi dmnAuth = WnLoginApiMaker.forHydrate()
                    .make(io, env, options);
                if (log.isInfoEnabled()) {
                    log.infof("Login as sub-user: %s", account);
                }
                WnUser user = dmnAuth.checkUser(account);
                if (log.isInfoEnabled()) {
                    log.infof("sub user : %s", user.toString());
                }
                // -----------------------------------------
                // 按登录模式认证：密码 / OTP / 双重
                boolean passwdOk = mode != WnAppModuleLoginMode.OTP_ONLY
                                   && user.isMatchedRawPasswd(passwd);
                boolean otpOk = false;
                // 需要 OTP 补充认证的情况：
                // AND（密码已过）/ OR（密码没过）/ OTP_ONLY
                if ((mode == WnAppModuleLoginMode.PASSWD_AND_OTP && passwdOk)
                    || (mode == WnAppModuleLoginMode.PASSWD_OR_OTP && !passwdOk)
                    || mode == WnAppModuleLoginMode.OTP_ONLY) {
                    String otpErr = this._check_otp(this._get_otps(), user);
                    // OR 模式没填验证码不算 OTP 失败，落到密码错误
                    if (null == otpErr) {
                        otpOk = true;
                    } else if (!(mode == WnAppModuleLoginMode.PASSWD_OR_OTP
                                 && "e.auth.login.otp.required".equals(otpErr))) {
                        reo = Er.create(otpErr);
                        // AND 模式：OTP 不过就不能登录（此时 passwdOk 为 true）
                        passwdOk = false;
                    }
                }
                // -----------------------------------------
                // 登录成功
                if (passwdOk || otpOk) {
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check passwd");
                    }

                    // 获取会话时长设置
                    int se_du = dmnAuth.getSessionDuration();

                    // 注册新会话
                    se = dmnAuth.createSession(user, Wn.SET_LOGIN_APP, se_du);

                    // 确保用户是可以访问域主目录的
                    site.assertHomeAccessable(se);
                    if (log.isInfoEnabled()) {
                        log.infof("OK: check_home_accessable");
                    }

                    // 更新会话元数据
                    if (log.isInfoEnabled()) {
                        log.infof("OK: create session: %s : %s",
                                  se.getTicket(),
                                  se.getMyName());
                    }

                    // 获取重定向路径
                    String appName = se.getEnv()
                        .getString("OPEN", "wn.manager");
                    redirectPath = "/a/open/" + appName;

                    if (log.isInfoEnabled()) {
                        log.infof(">>: %s", redirectPath);
                    }

                    // 准备返回值
                    reo = se.toBean(dmnAuth);
                }
                // -----------------------------------------
                // 登录失败
                else {
                    if (log.isInfoEnabled()) {
                        log.infof("KO: passwd fail");
                    }
                    // OTP 错误已记录在 reo 里（如 otp.required），否则报密码错
                    if (null == reo) {
                        reo = Er.create("e.auth.login.invalid.passwd");
                    }
                }
            }

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
     * 域用户登录：账户在系统用户库里，按登录模式组合密码 / OTP 认证
     *
     * @param mode
     *            登录模式
     * @return 登录成功的会话
     */
    private WnSession __login_as_domain_user(WnAppModuleLoginMode mode) {
        if (log.isInfoEnabled()) {
            log.infof("Login as domain-user: %s", account);
        }
        // 先尝试密码（OTP_ONLY 模式跳过）
        WnSession se = null;
        WebException passwdErr = null;
        if (mode != WnAppModuleLoginMode.OTP_ONLY) {
            try {
                se = auth.loginByPassword(account, passwd);
            }
            catch (WebException e) {
                passwdErr = e;
            }
        }
        // PASSWD_AND_OTP：密码过了还必须验 OTP
        if (null != se && mode == WnAppModuleLoginMode.PASSWD_AND_OTP) {
            String otpErr = this._check_otp(this._get_otps(), se.getUser());
            // OTP 不过：回收会话，报错
            if (null != otpErr) {
                auth.removeSession(se);
                throw Er.create(otpErr);
            }
            return se;
        }
        if (null != se) {
            return se;
        }
        // PASSWD_OR_OTP / OTP_ONLY：密码没过（或不试密码），走 OTP
        if (mode == WnAppModuleLoginMode.PASSWD_OR_OTP
            || mode == WnAppModuleLoginMode.OTP_ONLY) {
            // 没填验证码：OTP_ONLY 明确报 required；OR 模式报原密码错误
            if (Ws.isBlank(otpCode)) {
                if (mode == WnAppModuleLoginMode.OTP_ONLY) {
                    throw Er.create("e.auth.login.otp.required");
                }
                throw passwdErr;
            }
            WnUser user = auth.checkUser(account);
            String otpErr = this._check_otp(this._get_otps(), user);
            if (null != otpErr) {
                throw Er.create(otpErr);
            }
            // 免密建会话
            int se_du = auth.getSessionDuration();
            return auth.createSession(user, Wn.SET_LOGIN_APP, se_du);
        }
        // PASSWD_ONLY：原样抛出密码错误
        throw passwdErr;
    }

    @Override
    protected String _otp_usr_home() {
        WnLoginSite site = WnLoginSite.create(io, siteId, hostName);
        return null == site ? null : site.getDomainHomePath();
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

}
