package org.nutz.walnut.impl.box.cmd;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

/**
 * 转换图片
 * 
 * @author pw
 * 
 */
public class cmd_chimg extends cmd_image {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "z");

        if (params.vals.length == 0)
            throw Er.create("e.cmd.chimg.noinput");

        // 输入对象
        WnObj inObj = Wn.checkObj(sys, params.vals[0]);

        // 必须是文件
        if (!inObj.isFILE()) {
            throw Er.create("e.cmd.chimg.notfile", inObj.path());
        }

        // 必须是图片
        if (!inObj.mime().startsWith("image/")) {
            throw Er.create("e.cmd.chimg.notimage", inObj.path());
        }

        // -s 大小
        int sw = 0;
        int sh = 0;
        if (params.has("s")) {
            String pa_s = params.get("s");
            Matcher m = Pattern.compile("^(\\d+)[xX](\\d+)$").matcher(pa_s);
            if (m.find()) {
                sw = Integer.parseInt(m.group(1));
                sh = Integer.parseInt(m.group(2));
            }
        }

        // -z 保持比例
        Color bgcolor = null;
        boolean scaleZoom = params.has("z");
        if (scaleZoom) {
            // -bg 背景颜色
            String pa_bg = params.get("bg");
            if (!Strings.isBlank(pa_bg)) {
                bgcolor = Colors.as(pa_bg);
            }
        }
        // 旋转
        int degree = params.getInt("ro", 0);

        // 准备输出对象
        WnObj outObj = null;

        // 看看输出到哪里呀
        String pa_o = params.get("o", params.vals.length > 1 ? params.vals[1] : null);

        // 没指定新路径则替换原来的
        if (Strings.isBlank(pa_o)) {
            outObj = inObj;
        }
        // 否则就试图创建这个对象
        else {
            String outPath = Wn.normalizeFullPath(pa_o, sys);
            if (sys.io.exists(null, outPath)) {
                outObj = sys.io.fetch(null, outPath);
            } else {
                outObj = sys.io.createIfNoExists(null, outPath, WnRace.FILE);
            }
            // 如果是目录，就创建一个文件对象
            if (outObj.isDIR()) {
                outObj = sys.io.createIfNoExists(outObj, inObj.name(), WnRace.FILE);
            }
        }

        // 开始处理
        BufferedImage inImg = Images.read(sys.io.getInputStream(inObj, 0));
        BufferedImage outImg = null;

        // 缩放图片
        if (sw > 0 && sh > 0) {
            if (scaleZoom) {
                outImg = Images.zoomScale(inImg, sw, sh, bgcolor);
            }
            // 剪裁缩放
            else {
                outImg = Images.clipScale(inImg, sw, sh);
            }
            // 作为下一个输入的源
            inImg = outImg;
        }

        // 旋转图片
        if (degree != 0) {
            outImg = Images.rotate(inImg, degree);
        }

        // 写入outObj中
        Images.writeAndClose(outImg, outObj.type(), sys.io.getOutputStream(outObj, 0));
    }

}
