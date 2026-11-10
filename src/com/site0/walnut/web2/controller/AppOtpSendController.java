package com.site0.walnut.web2.controller;

import java.nio.charset.StandardCharsets;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.login.usr.WnUser;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.otp.WnOTPService;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.controller.app.WnOcmInfo;

public class AppOtpSendController extends AbstractAppAuthController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        try {
            NutMap params = params(ctx.request());
            String account = params.getString("account");
            WnOcmInfo ocm = get_ocm_info(ctx, arg(ctx, 0), account);
            return send(ctx, ocm, params);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

    protected WnMvcView send(WnMvcContext ctx, WnOcmInfo ocm, NutMap params) {
        String otpConfigName = params.getString("otp");
        String captchaName = params.getString("captcha");
        String ticket = params.getString("ticket");
        String account = params.getString("account");
        cors(ctx);
        if (options(ctx))
            return null;
        String reJson;
        try {
            if (Ws.isBlank(account)) {
                reJson = __fail_json("e.otp.need_account", null);
                return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
            }
            if (Ws.isBlank(ticket)) {
                reJson = __fail_json("e.otp.need_ticket", null);
                return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
            }
            WnCaptchaApi api = createCaptcha(ctx, ocm, captchaName);
            if (!api.checkTicket(ticket, account)) {
                reJson = __fail_json("e.captcha.ticket.invalid", null);
                return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
            }
            WnUser user = ocm.getUser();
            if (null == user) {
                reJson = __fail_json("e.otp.account.noexists", account);
                return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
            }
            String email = user.getEmail();
            if (Ws.isBlank(email)) {
                reJson = __fail_json("e.otp.account.no_email", account);
                return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
            }
            WnOTPService otps = __check_otps(ctx, ocm, otpConfigName);
            WnOTPBean otp = otps.create(email);
            otps.save(otp);
            var sendRe = otps.send(otp);
            if (!sendRe.isOk()) {
                String errCode = Strings.sBlank(sendRe.getErrCode(), "e.otp.send");
                reJson = __fail_json(errCode, sendRe.getMsg());
                return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
            }
            NutMap re = new NutMap("ok", true);
            reJson = Json.toJson(re, JsonFormat.compact());
        }
        catch (Exception e) {
            if (log.isWarnEnabled())
                log.warn("Fail to send OTP", e);
            reJson = __fail_json("e.otp.unavailable", e.getMessage());
        }
        return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
    }

    private WnOTPService __check_otps(WnMvcContext ctx, WnOcmInfo ocm, String otpConfigName) {
        String ocName = Strings.sBlank(otpConfigName, "default");
        NutMap sessionVars = new NutMap();
        sessionVars.put("HOME", ocm.getConfHomePath());
        return new WnOTPService(io(ctx), sessionVars, ocName);
    }

    private String __fail_json(String errCode, String msg) {
        NutMap re = new NutMap().setv("ok", false).setv("errCode", errCode);
        if (!Ws.isBlank(msg))
            re.setv("msg", msg);
        return Json.toJson(re, JsonFormat.compact());
    }
}
