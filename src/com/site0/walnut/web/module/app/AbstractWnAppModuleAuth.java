package com.site0.walnut.web.module.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.security.otp.WnOTPService;
import com.site0.walnut.util.WnRun;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.WnConfig;

/**
 * 登录服务类的公共基类：收编两个登录服务类（系统密码登录 / 域站点密码登录）的
 * 公共属性，并提供 OTP 动态密码的懒构建与校验、登录后 profile 初始化。
 *
 * <p>
 * 只持有运行所需的协作对象（{@link WnLoginApi} / {@link WnIo} /
 * {@link WnConfig} / {@link WnRun}），不依赖具体的 Module 类，耦合保持在
 * 内核工具层。OTP 服务实例按请求懒构建一次（配置在
 * <code>~/.otp/{name}.json5</code>），构建失败不抛错，返回
 * <code>null</code> 交由 {@link #_check_otp(WnOTPService, WnUser)} 报
 * <code>e.auth.login.otp.unavailable</code>——只有模式真的需要 OTP 时
 * 才会走到这里，没必要用 OTP 的请求完全不碰 OTP 服务。
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class AbstractWnAppModuleAuth {

    private static final Log log = Wlog.getAPP();

    /**
     * 登录接口（系统用户库，或域站点用户库）
     */
    protected WnLoginApi auth;

    /**
     * IO 运行时
     */
    protected WnIo io;

    /**
     * Web 配置（读取登录模式等）
     */
    protected WnConfig conf;

    /**
     * 内核运行器：执行登录后的 ~/.profile 初始化脚本
     */
    protected WnRun run;

    /**
     * 用户输入的密码
     */
    protected String passwd;

    /**
     * 返回的会话是否用 Ajax 形式包裹
     */
    protected boolean ajax;

    protected HttpServletRequest req;

    protected HttpServletResponse resp;

    /**
     * OTP 配置名（对应 <code>~/.otp/{name}.json5</code>），缺省 "default"
     */
    protected String otpConfigName;

    /**
     * 用户回填的 OTP 验证码
     */
    protected String otpCode;

    /**
     * 本次请求懒构建的 OTP 服务实例
     */
    private WnOTPService otps;

    /**
     * @return OTP 配置所属用户的家目录（作为 sessionVars 的 HOME，
     *         OTP 配置实际位于其下的 <code>~/.otp/</code> 里）
     */
    protected abstract String _otp_usr_home();

    /**
     * 懒构建 OTP 服务：构建失败（配置缺失、Redis 不通等）返回
     * <code>null</code>，调用方通过 {@link #_check_otp(WnOTPService, WnUser)}
     * 得到 <code>e.auth.login.otp.unavailable</code> 错误
     *
     * @return OTP 服务实例，构建失败返回 <code>null</code>
     */
    protected WnOTPService _get_otps() {
        if (null != this.otps) {
            return this.otps;
        }
        String otpName = Strings.sBlank(this.otpConfigName, "default");
        try {
            String otpUsrHome = this._otp_usr_home();
            NutMap sessionVars = new NutMap();
            sessionVars.put("HOME", otpUsrHome);
            this.otps = new WnOTPService(this.io, sessionVars, otpName);
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warnf("Fail to create WnOTPService[%s] : %s",
                          otpName,
                          e.getMessage());
            }
            this.otps = null;
            return null;
        }
        return this.otps;
    }

    /**
     * 校验用户回填的 OTP 验证码（一次性消费，验证通过即删码）。
     *
     * <p>
     * 校验的账号取用户的邮箱（OTP 的 account 就是收件地址），不是登录名。
     *
     * @param otps
     *            OTP 服务实例（可为 <code>null</code>，表示服务不可用）
     * @param user
     *            登录用户
     * @return 错误码，<code>null</code> 表示验证通过
     */
    protected String _check_otp(WnOTPService otps, WnUser user) {
        // 没填验证码
        if (Ws.isBlank(this.otpCode)) {
            return "e.auth.login.otp.required";
        }
        // 用户没设邮箱，无法 OTP 认证
        String email = user.getEmail();
        if (Ws.isBlank(email)) {
            return "e.auth.login.otp.no_email";
        }
        // 服务器没配 OTP 服务
        if (null == otps) {
            return "e.auth.login.otp.unavailable";
        }
        // 验证码不对（不存在 / 已过期 / 已作废 / 超重试上限）
        if (!otps.check(email, this.otpCode)) {
            return "e.auth.login.otp.invalid";
        }
        return null;
    }

    /**
     * 登录成功后，执行用户家目录下的 <code>~/.profile</code> 初始化脚本
     *
     * @param se
     *            会话（为 <code>null</code> 时什么都不做）
     */
    protected void _try_run_profile(WnSession se) {
        this.run.tryRunProfile(se);
    }

    public WnLoginApi getAuth() {
        return auth;
    }

    public void setAuth(WnLoginApi auth) {
        this.auth = auth;
    }

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public WnConfig getConf() {
        return conf;
    }

    public void setConf(WnConfig conf) {
        this.conf = conf;
    }

    public WnRun getRun() {
        return run;
    }

    public void setRun(WnRun run) {
        this.run = run;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public boolean isAjax() {
        return ajax;
    }

    public void setAjax(boolean ajax) {
        this.ajax = ajax;
    }

    public HttpServletRequest getReq() {
        return req;
    }

    public void setReq(HttpServletRequest req) {
        this.req = req;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }

    public String getOtpConfigName() {
        return otpConfigName;
    }

    public void setOtpConfigName(String otpConfigName) {
        this.otpConfigName = otpConfigName;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

}
