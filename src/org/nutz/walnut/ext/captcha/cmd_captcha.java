package org.nutz.walnut.ext.captcha;

import java.io.ByteArrayOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.ZParams;

import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;

public class cmd_captcha extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        final ZParams params = ZParams.parse(args, "^(bg|noise|border|fisheye)$");
        int w = params.getInt("w", 120);
        int h = params.getInt("h", 30);
        Captcha.Builder builder = new Captcha.Builder(w, h);
        builder.addText(() -> {
            if (params.has("text"))
                return params.get("text");
            else
                return R.captchaNumber(params.getInt("textlen", 4));
        });
        if (params.is("bg"))
            builder.addBackground();
        if (params.is("noise"))
            builder.addNoise();
        if (params.is("border"))
            builder.addBorder();
        if (params.is("fisheye"))
            builder.gimp(new FishEyeGimpyRenderer());
        Captcha captcha = builder.build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Images.write(captcha.getImage(), "png", out);

        if ("png".equals(params.get("out"))) {
            sys.out.write(out.toByteArray());
        } else {
            NutMap re = new NutMap();
            re.put("text", captcha.getAnswer());
            re.put("png", Base64.encodeBase64URLSafeString(out.toByteArray()));
            sys.out.println(Json.toJson(re));

        }
    }

}
