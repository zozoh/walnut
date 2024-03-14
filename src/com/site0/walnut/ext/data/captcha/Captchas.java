package com.site0.walnut.ext.data.captcha;

import java.io.ByteArrayOutputStream;

import org.nutz.img.Images;
import org.nutz.lang.Maths;
import cn.apiclub.captcha.Captcha;
import cn.apiclub.captcha.gimpy.FishEyeGimpyRenderer;

public abstract class Captchas {

    /**
     * 生成模式: 添加背景
     */
    public static final int BG = 1;

    /**
     * 生成模式: 添加噪点
     */
    public static final int NOISE = 1 << 1;

    /**
     * 生成模式: 添加边框
     */
    public static final int BORDER = 1 << 2;

    /**
     * 生成模式: 鱼眼效果
     */
    public static final int FISHEYE = 1 << 3;

    /**
     * 组合生成模式: BETTER
     */
    public static final int MODE_BETTER = NOISE | FISHEYE;

    /**
     * 生成验证码图片(PNG)
     * 
     * @param text
     *            文字
     * @param w
     *            验证码图片宽度
     * @param h
     *            验证码图片高度
     * @param mode
     *            生成模式
     * @return PNG 图片的内容字节码
     */
    public static byte[] genPng(String text, int w, int h, int mode) {
        Captcha.Builder builder = new Captcha.Builder(w, h);
        builder.addText(() -> {
            return text;
        });
        if (Maths.isMask(mode, BG))
            builder.addBackground();
        if (Maths.isMask(mode, NOISE))
            builder.addNoise();
        if (Maths.isMask(mode, BORDER))
            builder.addBorder();
        if (Maths.isMask(mode, FISHEYE))
            builder.gimp(new FishEyeGimpyRenderer());
        Captcha captcha = builder.build();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Images.write(captcha.getImage(), "png", out);

        return out.toByteArray();
    }

    // 阻止实例化
    private Captchas() {}
}
