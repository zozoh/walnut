package com.site0.walnut.ext.data.www.hdl;

import java.io.IOException;
import java.io.OutputStream;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnCaptcha;
import com.site0.walnut.api.auth.WnCaptchaService;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.captcha.Captchas;
import com.site0.walnut.ext.data.www.cmd_www;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.WnHttpResponseWriter;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(http|better|ajax|robot)$")
public class www_captcha implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/场景/账户
        WnObj oWWW = cmd_www.checkSite(sys, hc);
        String scene = hc.params.val_check(1);
        String account = hc.params.val_check(2);
        // -------------------------------
        WnWebService webs = new WnWebService(sys, oWWW);
        WnCaptchaService capApi = webs.getCaptchaApi();
        // -------------------------------
        Object out = null;
        // -------------------------------
        // 校验验证码
        if (hc.params.has("verify")) {
            String code = hc.params.getString("verify");
            // 尝试校验一下 ...
            if (capApi.removeCaptcha(scene, account, code)) {
                out = Ajax.ok();
            }
            // 出错了
            else {
                out = Ajax.fail().setErrCode("e.www.invalid.captcha");
            }
        }
        // -------------------------------
        // 生成验证码
        else {
            String as = hc.params.getString("as", "json");
            // 短信或者邮箱验证码，需要先校验一下机器人
            if (!hc.params.is("robot")) {
                if ("sms".equals(as) || "email".equals(as)) {
                    String cap = hc.params.check("cap");
                    String capScene = hc.params.get("capscene", "robot");
                    // 看看是否有效
                    if (!webs.getCaptchaApi().removeCaptcha(capScene, account, cap)) {
                        AjaxReturn re = Ajax.fail().setErrCode("e.www.invalid.captcha");
                        sys.out.println(Json.toJson(re, hc.jfmt));
                        return;
                    }
                }
            }
            // 生成验证码，如果是短信或者
            out = gen_vcode(sys, hc, scene, account, capApi, as);
        }
        // -------------------------------
        // 输出验证码
        if (null != out) {
            if (!(out instanceof AjaxReturn)) {
                // 默认是JSON 方式
                if (hc.params.is("ajax")) {
                    out = Ajax.ok().setData(out);
                }
            }
            // 输出吧
            sys.out.println(Json.toJson(out, hc.jfmt));
        }
        // -------------------------------
    }

    private Object gen_vcode(WnSystem sys,
                             JvmHdlContext hc,
                             String scene,
                             String account,
                             WnCaptchaService capApi,
                             String as)
            throws IOException {
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
        capApi.saveCaptcha(cap);
        // -------------------------------
        // 准备输出
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
                WnHttpResponseWriter resp = new WnHttpResponseWriter();
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
            // 总之不要继续输出了
            return null;
        }
        // -------------------------------
        // 最后肯定是要输出 json 的，那么是否输出 code 则作为一个过滤条件
        // -------------------------------
        // SMS 方式
        if ("sms".equals(as)) {
            // 发送短信
            NutMap cc = cap.toMeta("account");
            if (hc.params.has("by")) {
                cc.put("scene", hc.params.get("by"));
            }
            String cmdTmpl = "sms send -r ${account} -t 'i18n:${scene}'"
                             + " -lang '${lang?zh-cn}'"
                             + " 'code:\"${code}\",min:${du_in_min},hour:${du_in_hr}'";
            String cmdText = WnTmpl.exec(cmdTmpl, cc);
            String re = sys.exec2(cmdText);
            NutMap reMap = Json.fromJson(NutMap.class, re);
            NutMap acMap = reMap.getAs(cap.getAccount(), NutMap.class);
            // 靠，发送失败
            if (null == acMap || !acMap.is("code", 0)) {
                return Ajax.fail()
                           .setErrCode("e.www.captcha.fail_send_by_sms")
                           .setData(acMap.get("msg"));
            }

            // 按照 JSON 输出，但是不带 code
            hc.jfmt.setLocked("^(code)$");
        }
        // -------------------------------
        // 邮件方式
        else if ("email".equals(as)) {
            // TODO 发送邮件
            // email -r zozoh@qq.com -s i18n:signup -tmpl i18n:signup -vars
            // 'code:"AABBCC", hour:1'
            NutMap cc = cap.toMeta("account");
            if (hc.params.has("by")) {
                cc.put("scene", hc.params.get("by"));
            }
            // String cmdTmpl = "email -r ${account} -s 'i18n:${scene}'"
            // + " -tmpl 'i18n:${scene}'"
            // + " -lang '${lang?zh-cn}'"
            // + " -vars 'code:\"${code}\",min:${du_in_min},hour:${du_in_hr}'";
            String cmdTmpl = "sendmail -lang '${lang?}' -ajax"
                             + " @to ${account}"
                             + " @tmpl '${scene}'"
                             + " @vars 'code:\"${code}\",min:${du_in_min},hour:${du_in_hr}'";
            String cmdText = WnTmpl.exec(cmdTmpl, cc);
            String re = sys.exec2(cmdText);
            NutMap reMap = Json.fromJson(NutMap.class, re);
            // 靠，发送失败
            if (null == reMap || !reMap.getBoolean("ok")) {
                return Ajax.fail().setErrCode("e.www.captcha.fail_send_by_email");
            }
            // 按照 JSON 输出，但是不带 code
            hc.jfmt.setLocked("^(code)$");
        }
        return cap;
    }

}
