package com.site0.walnut.web.module.app;

import com.site0.walnut.util.Ws;
import com.site0.walnut.log.WnLog;
import org.nutz.mvc.View;
import org.nutz.mvc.view.ServerRedirectView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.WebException;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxView;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
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
 * <li><code>OTP_ONLY</code>：只验 OTP，通过则免密建会话</li>
 * <li><code>PASSWD_AND_OTP</code>：密码和 OTP 双重认证。OTP 轻量优先验，密码
 * 其次，都过了才创建会话</li>
 * <li><code>PASSWD_OR_OTP</code>：提供了什么就验什么——密码不为空验密码，OTP
 * 不为空验 OTP，都过了才建会话；两个都为空报密码错</li>
 * </ul>
 *
 * <p>
 * OTP 配置在 root 用户家目录的 <code>~/.otp/{name}.json5</code>（
 * <code>ocm.confHomePath</code>，操作上下文由入口函数经
 * <code>AppModule.get_ocm_info</code> 构建传入）；校验账号取用户的邮箱（见
 * {@link AbstractWamAuth#_check_otp}）
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WamSysLoginByPassword extends AbstractWamAuth {

    private static final WnLog log = Wlog.getAPP();

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
            // 登录用户（不存在直接报错）
            WnUser user = sysAuth.checkUser(name);
            // 按登录模式认证：OTP 轻量优先，密码其次，都过了才建会话
            switch (mode) {
            case OTP_ONLY:
                this._assert_otp(user);
                break;
            case PASSWD_ONLY:
                this.__assert_passwd(user);
                break;
            case PASSWD_AND_OTP:
                this._assert_otp(user);
                this.__assert_passwd(user);
                break;
            case PASSWD_OR_OTP:
                // 提供了什么就验什么：密码不为空验密码，OTP 不为空验 OTP
                if (!Ws.isBlank(passwd)) {
                    this.__assert_passwd(user);
                }
                if (!Ws.isBlank(otpCode)) {
                    this._assert_otp(user);
                }
                // 两个都为空，报密码错（与历史行为一致）
                if (Ws.isBlank(passwd) && Ws.isBlank(otpCode)) {
                    throw Er.create("e.auth.login.Failed");
                }
                break;
            }
            // -----------------------------------------
            // 认证通过，创建会话（尽量晚创建）
            int se_du = sysAuth.getSessionDuration();
            WnSession se = sysAuth.createSession(user, Wn.SET_AUTH_PASS, se_du);
            // 尝试初始化会话
            this._try_run_profile(se);
            // 如果是 Ajax 视图
            if (ajax) {
                // 获取 cookie 模板
                Object reo = se.toBean(sysAuth);
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
            String redirectTo = Ws.sBlank(referer, "/");
            return new ServerRedirectView(redirectTo);
        }
    }

    /**
     * 断言密码校验通过，不过报 <code>e.auth.login.Failed</code>（与历史行为一致）
     *
     * @param user
     *            登录用户
     */
    private void __assert_passwd(WnUser user) {
        if (user.isMatchedRawPasswd(passwd)) {
            return;
        }
        throw Er.create("e.auth.login.Failed");
    }

    private View __get_session_default_view(WnSession se) {
        String appName = se.getEnv().getString("OPEN", "wn.console");
        String url = "/a/open/" + appName;
        Object reData = se.toBean(sysAuth);
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
