package com.site0.walnut.web2.controller;

import java.nio.charset.StandardCharsets;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.util.Ws;
import com.site0.walnut.web2.controller.app.WnOcmInfo;

public class AppCaptchaVerifyController extends AbstractAppAuthController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        try {
            // 2026-11-10 用户修正：前端实际协议为 POST JSON 载荷；参数从 query/form/JSON body
            // 合并（旧默认适配器语义，query 优先、body 补齐），不再只看 query string
            NutMap map = params(ctx.request());
            String domain = map.getString("domain");
            if (!Strings.equals(domain, arg(ctx, 0))) {
                String result = Json.toJson(new NutMap().setv("ok", false)
                        .setv("errCode", "e.captcha.domain.mismatch"), JsonFormat.compact());
                return WnMvcView.raw(result.getBytes(StandardCharsets.UTF_8), "text/json");
            }
            Object data = map.get("data");
            String captchaData = null;
            if (null != data)
                captchaData = data instanceof CharSequence ? data.toString() : Json.toJson(data, JsonFormat.compact());
            String account = map.getString("account");
            WnOcmInfo ocm = get_ocm_info(ctx, domain, account);
            String captchaName = map.getString("captcha");
            String captchaId = map.getString("id");
            String reJson;
            try {
                if (Ws.isBlank(account)) {
                    reJson = captchaFailure("e.captcha.need_account", null);
                    return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
                }
                WnCaptchaApi api = createCaptcha(ctx, ocm, captchaName);
                if (!api.check(captchaId, captchaData)) {
                    reJson = captchaFailure("e.captcha.invalid", null);
                    return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
                }
                String ticket = api.createTicket(account);
                NutMap result = new NutMap("ticket", ticket);
                NutMap re = new NutMap().setv("ok", true).setv("data", result);
                reJson = Json.toJson(re, JsonFormat.compact());
            }
            catch (Exception e) {
                if (log.isWarnEnabled())
                    log.warn("Fail to verify captcha", e);
                reJson = captchaFailure("e.captcha.unavailable", e);
            }
            return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }
}
