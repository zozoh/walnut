package com.site0.walnut.web2.controller;

import java.nio.charset.StandardCharsets;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.security.captcha.WnCaptchaApi;
import com.site0.walnut.security.captcha.WnCaptchaObj;
import com.site0.walnut.web2.controller.app.WnOcmInfo;

public class AppCaptchaGetController extends AbstractAppAuthController {
    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        cors(ctx);
        if (options(ctx))
            return WnMvcView.none();
        try {
            NutMap params = params(ctx.request());
            WnOcmInfo ocm = get_ocm_info(ctx, arg(ctx, 0), params.getString("account"));
            String captchaName = params.getString("captcha");
            String reJson;
            try {
                WnCaptchaApi api = createCaptcha(ctx, ocm, captchaName);
                WnCaptchaObj obj = api.generate();
                reJson = String.format("{\"ok\":true, \"data\":%s}", obj.toString());
            }
            catch (Exception e) {
                if (log.isWarnEnabled())
                    log.warn("Fail to generate captcha", e);
                reJson = captchaFailure("e.captcha.unavailable", e);
            }
            return WnMvcView.raw(reJson.getBytes(StandardCharsets.UTF_8), "text/json");
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }
}
