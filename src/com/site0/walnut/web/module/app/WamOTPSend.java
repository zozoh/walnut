package com.site0.walnut.web.module.app;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import com.site0.walnut.util.Ws;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.log.WnLog;
import org.nutz.mvc.View;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.login.usr.WnUser;
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
 * 发送前必须先通过人机校验：客户端用 captcha 换来的临时票据 （<code>ticket</code>，绑定账号、一次性、带过期）在这里消费——
 * <code>checkTicket(ticket, account)</code> 不过直接拒绝，不触发任何发送。 这一步挡的是机器人批量触发发信。
 *
 * <p>
 * 票据通过后：从 {@link WamOcmInfo#getUser()} 取用户（登录名解析，
 * captcha 阶段用户不存在时这里是 <code>null</code>，发送前必须存在），取其邮箱作为
 * OTP 收件账号（用户没设邮箱报错）；然后生成、存储、发送 OTP
 * （存储同账号旧码被覆盖，「一发一验」）。OTP 与 captcha 的配置目录取自
 * {@link WamOcmInfo#getConfHomePath()}（登录模式决定，规则见
 * <code>62-auth-with-captcha-otp.md</code>）。每次请求都现场构建服务实例，
 * 不做缓存，能始终读到最新的配置。
 *
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WamOTPSend {

    private static final WnLog log = Wlog.getAPP();

    private WnIo io;

    /**
     * 本次操作的信息：配置主目录、用户等（由入口函数经
     * <code>AppModule.get_ocm_info(domain, account)</code> 解析）
     */
    private WamOcmInfo ocm;

    /**
     * OTP 配置名（对应 <code>{confHome}/.otp/{name}.json5</code>），缺省 "default"
     */
    private String otpConfigName;

    /**
     * captcha 配置名（对应 <code>{confHome}/.captcha/{name}.json</code>），缺省
     * "default"
     */
    private String captchaName;

    /**
     * captcha verify 签发的临时票据（绑定账号，一次性，带过期）
     */
    private String ticket;

    /**
     * 登录名（用户名 / 手机 / 邮箱），OTP 发到其邮箱
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

            // 第二步：取用户邮箱作为 OTP 收件账号
            WnUser user = ocm.getUser();
            if (null == user) {
                reJson = this.__fail_json("e.otp.account.noexists", account);
                return new ViewWrapper(new RawView("json"), reJson);
            }
            String email = user.getEmail();
            if (Ws.isBlank(email)) {
                reJson = this.__fail_json("e.otp.account.no_email", account);
                return new ViewWrapper(new RawView("json"), reJson);
            }
            // 第三步：生成并存储 OTP（同账号旧码被覆盖，「一发一验」）
            WnOTPService otps = this.__check_otps();
            WnOTPBean otp = otps.create(email);
            otps.save(otp);
            // 第四步：发送
            AjaxReturn sendRe = otps.send(otp);
            if (!sendRe.isOk()) {
                String errCode = Ws.sBlank(sendRe.getErrCode(),
                                                "e.otp.send");
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
     * @return 当前登录模式下现场构建的 captcha 服务接口（用来校验票据）
     */
    private WnCaptchaApi __check_api() {
        String ccName = Ws.sBlank(captchaName, "default");
        NutMap vars = new NutMap();
        vars.put("HOME", ocm.getConfHomePath());
        WnCaptchaBuilder builder = new WnSysCaptchaBuilder(this.io, vars);
        return builder.build(ccName);
    }

    /**
     * @return 当前登录模式下现场构建的 OTP 服务实例
     */
    private WnOTPService __check_otps() {
        String ocName = Ws.sBlank(otpConfigName, "default");
        NutMap sessionVars = new NutMap();
        sessionVars.put("HOME", ocm.getConfHomePath());
        return new WnOTPService(this.io, sessionVars, ocName);
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

    public WnIo getIo() {
        return io;
    }

    public void setIo(WnIo io) {
        this.io = io;
    }

    public WamOcmInfo getOcm() {
        return ocm;
    }

    public void setOcm(WamOcmInfo ocm) {
        this.ocm = ocm;
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
