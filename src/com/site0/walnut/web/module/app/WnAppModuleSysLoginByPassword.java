package com.site0.walnut.web.module.app;

import org.nutz.lang.Strings;
import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxView;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.security.otp.WnOTPService;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.util.WnAppModuleLoginMode;
import com.site0.walnut.web.util.WnWeb;
import com.site0.walnut.web.view.WnAddCookieViewWrapper;

import redis.clients.jedis.exceptions.JedisException;

/**
 * 对应入口函数 <code>AppModule.sys_login_by_passwd</code> 的服务类
 *
 * <p>
 * 认证模式由 <code>web.properties</code> 的 <code>app-module-sys-login</code>
 * 决定（缺省 <code>PASSWD_ONLY</code>，与历史行为一致）：
 *
 * <ul>
 * <li><code>PASSWD_ONLY</code>：只验密码，与历史行为完全一致</li>
 * <li><code>PASSWD_AND_OTP</code>：密码和 OTP 双重认证。先验密码，密码过了
 * 还必须验 OTP，OTP 不过回收会话并报错</li>
 * <li><code>PASSWD_OR_OTP</code>：密码或 OTP 任一通过即可。密码没过（或未提供）
 * 再试 OTP，OTP 通过则免密建会话</li>
 * <li><code>OTP_ONLY</code>：只验 OTP，通过则免密建会话</li>
 * </ul>
 *
 * <p>
 * OTP 配置在 root 用户家目录的 <code>~/.otp/{name}.json5</code>；
 * 校验账号取用户的邮箱（见 {@link AbstractWnAppModuleAuth#_check_otp}）
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleSysLoginByPassword extends AbstractWnAppModuleAuth {

    private static final Log log = Wlog.getAPP();

    /**
     * 登录名（用户名 / 手机 / 邮箱）
     */
    private String name;

    /**
     * 来源 URL（登录失败时跳回）
     */
    private String referer;

    public View invoke() {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        // 登录模式
        WnAppModuleLoginMode mode = conf.getSysLoginMode();
        // 尝试用系统账号登录
        try {
            WnSession se = null;
            WebException passwdErr = null;

            // 第一步：先尝试密码（OTP_ONLY 模式跳过）
            if (mode != WnAppModuleLoginMode.OTP_ONLY) {
                try {
                    se = auth.loginByPassword(name, passwd);
                }
                catch (WebException e) {
                    passwdErr = e;
                }
            }

            // PASSWD_AND_OTP：密码过了还必须验 OTP
            if (null != se && mode == WnAppModuleLoginMode.PASSWD_AND_OTP) {
                WnOTPService otps = this._get_otps();
                WnUser user = se.getUser();
                String otpErr = this._check_otp(otps, user);
                // OTP 不过：回收会话，报错
                if (null != otpErr) {
                    auth.removeSession(se);
                    throw Er.create(otpErr);
                }
            }

            // PASSWD_OR_OTP / OTP_ONLY：密码没过（或不试密码），走 OTP
            if (null == se
                && (mode == WnAppModuleLoginMode.PASSWD_OR_OTP
                    || mode == WnAppModuleLoginMode.OTP_ONLY)) {
                se = this.__login_by_otp(mode, passwdErr);
            }

            // 进行到这里还没会话，原样抛出密码错误
            // （PASSWD_ONLY / PASSWD_AND_OTP 密码错均走到这里，行为与历史一致）
            if (null == se) {
                throw passwdErr;
            }

            // 尝试初始化会话
            this._try_run_profile(se);
            // 如果是 Ajax 视图
            if (ajax) {
                // 获取 cookie 模板
                Object reo = se.toBean(auth);
                return new ViewWrapper(new WnAddCookieViewWrapper(conf,
                                                                  new AjaxView(),
                                                                  null),
                                       reo);
            }
            // 直接跳转到用户的主应用
            return this.__get_session_default_view(se);
        }
        // 出错了
        catch (WebException e) {
            Throwable eCause = e.getCause();
            if (null != eCause && (eCause instanceof JedisException)) {
                if (log.isWarnEnabled()) {
                    log.warn("Jedis error", eCause);
                }
            }
            Object reo = Ajax.fail()
                .setErrCode(e.getKey())
                .setData(e.getReason());
            // 返回视图
            if (ajax) {
                return new ViewWrapper(new AjaxView(), reo);
            }
            // 返回到原先地址
            String redirectTo = Strings.sBlank(referer, "/");
            return new ServerRedirectView(redirectTo);
        }
    }

    /**
     * OTP 登录路径：免密验证 OTP，通过则创建会话
     *
     * @param mode
     *            登录模式（PASSWD_OR_OTP / OTP_ONLY）
     * @param passwdErr
     *            之前密码登录的错误（OR 模式 OTP 未提供时原样报出）
     * @return 新会话
     */
    private WnSession __login_by_otp(WnAppModuleLoginMode mode, WebException passwdErr) {
        // 没填验证码：OTP_ONLY 明确报 required；OR 模式报原密码错误
        if (Ws.isBlank(otpCode)) {
            if (mode == WnAppModuleLoginMode.OTP_ONLY) {
                throw Er.create("e.auth.login.otp.required");
            }
            throw passwdErr;
        }
        // 用户必须存在，并取其邮箱作为 OTP 账号
        WnUser user = auth.checkUser(name);
        WnOTPService otps = this._get_otps();
        String otpErr = this._check_otp(otps, user);
        if (null != otpErr) {
            throw Er.create(otpErr);
        }
        // 免密建会话
        int se_du = auth.getSessionDuration();
        return auth.createSession(user, Wn.SET_LOGIN_APP, se_du);
    }

    @Override
    protected String _otp_usr_home() {
        WnUser root = auth.checkUser("root");
        return root.getHomePath();
    }

    private View __get_session_default_view(WnSession se) {
        String appName = se.getEnv().getString("OPEN", "wn.console");
        String url = "/a/open/" + appName;
        Object reData = se.toBean(auth);
        return new ViewWrapper(new WnAddCookieViewWrapper(conf, url), reData);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

}
