package com.site0.walnut.web.module.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.mvc.View;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.WnLoginApi;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaBuilder;
import com.site0.walnut.security.captcha.impl.WnSysCaptchaBuilder;
import com.site0.walnut.security.otp.WnOTPService;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.util.Wlog;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web.util.WnWeb;

/**
 * 对应入口函数 <code>AppModule.otp_send</code> 的服务类：发送 OTP 动态密码。
 *
 * <p>
 * 发送前必须先通过人机校验：客户端用 captcha 换来的临时票据
 * （<code>ticket</code>，绑定账号、一次性、带过期）在这里消费——
 * <code>checkTicket(ticket, account)</code> 不过直接拒绝，不触发任何发送。
 * 这一步挡的是机器人批量触发发信。
 *
 * <p>
 * 票据通过后，生成、存储、发送 OTP 三步走（存储同账号旧码被覆盖，
 * 「一发一验」）：OTP 与 captcha 的配置都取自域（系统用户名）的家目录
 * ——<code>~/.otp/{name}.json5</code> 与 <code>~/.captcha/{name}.json</code>，
 * 域为空表示 root 用户。每次请求都现场构建服务实例，不做缓存，
 * 能始终读到最新的域配置。
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnAppModuleOTPSend {

    private static final Log log = Wlog.getAPP();

    private WnLoginApi auth;

    private WnIo io;

    /**
     * 域（系统用户名），空表示 root 用户
     */
    private String domain;

    /**
     * OTP 配置名（对应 <code>~/.otp/{name}.json5</code>），缺省
     * "default"
     */
    private String otpConfigName;

    /**
     * captcha 配置名（对应 <code>~/.captcha/{name}.json</code>），缺省
     * "default"
     */
    private String captchaName;

    /**
     * captcha verify 签发的临时票据（绑定账号，一次性，带过期）
     */
    private String ticket;

    /**
     * 接收 OTP 的账号（手机号或邮箱，邮件发送时它就是收件人）
     */
    private String account;

    private HttpServletRequest req;

    private HttpServletResponse resp;

    /**
     * 发送一份 OTP 动态密码：先消费人机票据，再生成、存储、发送
     *
     * @return 成功 <code>{"ok":true}</code>；失败
     *         <code>{"ok":false,"errCode":...}</code>
     */
    public View send() {
        // 允许跨域
        WnWeb.setCrossDomainHeaders("*", (headName, headValue) -> {
            resp.setHeader(WnWeb.niceHeaderName(headName), headValue);
        });
        // 对于 options 放过
        if (WnWeb.isRequestOptions(req)) {
            return null;
        }
        String reJson;
        try {
            // 票据和账号缺一不可
            if (Ws.isBlank(account)) {
                reJson = this.__fail_json("e.otp.need_account", null);
                return new ViewWrapper(new RawView("json"), reJson);
            }
            if (Ws.isBlank(ticket)) {
                reJson = this.__fail_json("e.otp.need_ticket", null);
                return new ViewWrapper(new RawView("json"), reJson);
            }
            // 第一步：消费人机票据，证明不是机器人
            WnCaptchaApi api = this.__check_api();
            if (!api.checkTicket(ticket, account)) {
                reJson = this.__fail_json("e.captcha.ticket.invalid", null);
                return new ViewWrapper(new RawView("json"), reJson);
            }
            // 第二步：生成并存储 OTP（同账号旧码被覆盖，「一发一验」）
            WnOTPService otps = this.__check_otps();
            WnOTPBean otp = otps.create(account);
            otps.save(otp);
            // 第三步：发送
            AjaxReturn sendRe = otps.send(otp);
            if (!sendRe.isOk()) {
                String errCode = Strings.sBlank(sendRe.getErrCode(), "e.otp.send");
                reJson = this.__fail_json(errCode, sendRe.getMsg());
                return new ViewWrapper(new RawView("json"), reJson);
            }
            AjaxReturn re = Ajax.ok();
            reJson = Json.toJson(re, JsonFormat.compact());
        }
        catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Fail to send OTP", e);
            }
            reJson = this.__fail_json("e.otp.unavailable", e.getMessage());
        }
        return new ViewWrapper(new RawView("json"), reJson);
    }

    /**
     * @return 当前域现场构建的 captcha 服务接口（用来校验票据）
     */
    private WnCaptchaApi __check_api() {
        String name = Strings.sBlank(captchaName, "default");
        String usrName = Strings.sBlank(domain, "root");
        String usrHome = this.auth.checkUser(usrName).getHomePath();
        NutMap vars = new NutMap();
        vars.put("HOME", usrHome);
        WnCaptchaBuilder builder = new WnSysCaptchaBuilder(this.io, vars);
        return builder.build(name);
    }

    /**
     * @return 当前域现场构建的 OTP 服务实例
     */
    private WnOTPService __check_otps() {
        String name = Strings.sBlank(otpConfigName, "default");
        String usrName = Strings.sBlank(domain, "root");
        String usrHome = this.auth.checkUser(usrName).getHomePath();
        NutMap sessionVars = new NutMap();
        sessionVars.put("HOME", usrHome);
        return new WnOTPService(this.io, sessionVars, name);
    }

    /**
     * @param errCode
     *            错误码
     * @param msg
     *            错误消息（可为 <code>null</code>）
     * @return 失败 JSON 字符串
     */
    private String __fail_json(String errCode, String msg) {
        AjaxReturn re = Ajax.fail().setErrCode(errCode);
        if (!Ws.isBlank(msg)) {
            re.setMsg(msg);
        }
        return Json.toJson(re, JsonFormat.compact());
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

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getOtpConfigName() {
        return otpConfigName;
    }

    public void setOtpConfigName(String otpConfigName) {
        this.otpConfigName = otpConfigName;
    }

    public String getCaptchaName() {
        return captchaName;
    }

    public void setCaptchaName(String captchaName) {
        this.captchaName = captchaName;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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

}
