package org.nutz.walnut.ext.data.captcha;

import org.apache.commons.codec.binary.Base64;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

public class cmd_captcha extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        final ZParams params = ZParams.parse(args, "^(bg|noise|border|fisheye)$");

        // 宽高
        int w = params.getInt("w", 120);
        int h = params.getInt("h", 30);

        // 验证码文字
        String text = params.get("text");
        if (Strings.isBlank(text)) {
            text = R.captchaNumber(params.getInt("textlen", 4));
            ;
        }

        // 生成模式
        int mode = 0;
        if (params.is("bg"))
            mode |= Captchas.BG;
        if (params.is("noise"))
            mode |= Captchas.NOISE;
        if (params.is("border"))
            mode |= Captchas.BORDER;
        if (params.is("fisheye"))
            mode |= Captchas.FISHEYE;

        // 生成字节码
        byte[] bs = Captchas.genPng(text, w, h, mode);

        // 输出成 PNG 内容
        if ("png".equals(params.get("out"))) {
            sys.out.write(bs);
        }
        // 输出成 JSON
        else {
            NutMap re = new NutMap();
            re.put("text", text);
            re.put("png", Base64.encodeBase64URLSafeString(bs));
            sys.out.println(Json.toJson(re));
        }
    }

}
