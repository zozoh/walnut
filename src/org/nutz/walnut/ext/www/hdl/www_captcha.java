package org.nutz.walnut.ext.www.hdl;

import java.io.OutputStream;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.captcha.Captchas;
import org.nutz.walnut.ext.www.bean.WnCaptcha;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnHttpResponse;
import org.nutz.web.ajax.Ajax;

@JvmHdlParamArgs(value = "cqn", regex = "^(http|better|ajax)$")
public class www_captcha implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/场景/账户
        String site = hc.params.val_check(0);
        String scene = hc.params.val_check(1);
        String account = hc.params.val_check(2);
        WnObj oWWW = Wn.checkObj(sys, site);
        WnObj oDomain = Wn.checkObj(sys, "~/.domain");
        // -------------------------------
        // 生成验证码
        String type = hc.params.getString("type", "digital");
        int len = hc.params.getInt("len", 4);
        String code;
        // 字母验证码
        if ("alphabet".equals(type)) {
            code = R.captchaChar(len);
        }
        // 数字验证码
        else {
            code = R.captchaNumber(len);
        }
        // -------------------------------
        // 生成验证码对象
        int retry = hc.params.getInt("retry", 3);
        int du = hc.params.getInt("du", 10);
        WnCaptcha cap = new WnCaptcha(scene, account);
        cap.setCode(code);
        cap.setMaxRetry(retry);
        cap.setExpiFromNowByMin(du);
        // -------------------------------
        // 保存验证码
        WnWebService webs = new WnWebService(sys, oWWW, oDomain);
        webs.saveCaptcha(cap);
        // -------------------------------
        // 准备输出
        // -------------------------------
        String as = hc.params.getString("as", "json");
        // -------------------------------
        // 图片方式
        if ("png".equals(as)) {
            // 生成字节流
            int mode = Captchas.NOISE;
            if (hc.params.is("better")) {
                mode = Captchas.MODE_BETTER;
            }
            String size = hc.params.getString("size", "100x50");
            String[] ss = Strings.splitIgnoreBlank(size, "[xX*/]");
            int width = Integer.parseInt(ss[0]);
            int height = Integer.parseInt(ss[1]);
            byte[] buf = Captchas.genPng(code, width, height, mode);
            // HTTP 模式的包裹
            if (hc.params.is("http")) {
                WnHttpResponse resp = new WnHttpResponse();
                resp.setStatus(200);
                resp.setContentType("image/png");
                resp.prepare(buf);
                OutputStream ops = sys.out.getOutputStream();
                resp.writeTo(ops);
            }
            // 直接裸输出到标准输出
            else {
                sys.out.write(buf);
            }
        }
        // -------------------------------
        // 最后肯定是要输出 json 的，那么是否输出 code 则作为一个过滤条件
        // -------------------------------
        // SMS 方式
        if ("sms".equals(as)) {
            // 按照 JSON 输出，但是不带 code
            hc.jfmt.setLocked("^(code)$");
        }
        // -------------------------------
        // 邮件方式
        if ("sms".equals(as)) {
            // 按照 JSON 输出，但是不带 code
            hc.jfmt.setLocked("^(code)$");
        }
        // -------------------------------
        // 默认是JSON 方式
        Object out = cap;
        if (hc.params.is("ajax")) {
            out = Ajax.ok().setData(cap);
        }
        sys.out.println(Json.toJson(out, hc.jfmt));
        // -------------------------------
    }

}
