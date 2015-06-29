package org.nutz.walnut.impl.box.cmd;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;

import org.nutz.img.Images;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
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

    private String tmpDir = System.getProperty("java.io.tmpdir");

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "z");
        WnObj inObj = getImgObj(sys, params);
        WnObj outObj = null;
        if (inObj != null) {
            // /etc/thumbnail 或 $HOME/.thumbnail 下的图片, 不再生成对应的缩率图,
            if (inObj.path().startsWith("/etc/thumbnail") || inObj.path().contains("/.thumbnail")) {
                return;
            }
            // -s 大小
            int sw = 0;
            int sh = 0;
            String pa_s = params.get("s");
            if (Strings.isBlank(pa_s)) {
                return;
            } else {
                String[] wh = pa_s.toLowerCase().split("x");
                if (wh.length != 2) {
                    sys.err.print("size need 2 args, width and height");
                    return;
                }
                try {
                    sw = Integer.parseInt(wh[0]);
                    sh = Integer.parseInt(wh[1]);
                }
                catch (Exception e) {
                    sys.err.printf("size has err, please check %s", pa_s);
                    return;
                }
            }
            // -z 保持比例
            Color bgcolor = Color.white;
            boolean scaleZoom = params.has("z");
            if (scaleZoom) {
                // -bg 背景颜色
                String pa_bg = params.get("bg");
                if (!Strings.isBlank(pa_bg)) {
                    // TODO
                }
            }
            // -o 输出
            String pa_o = params.check("o");
            if (Strings.isBlank(pa_o)) {
                outObj = inObj;
            } else {
                if (pa_o.startsWith("id:")) {
                    String id = pa_o.substring("id:".length());
                    outObj = sys.io.checkById(id);
                } else {
                    String path = Wn.normalizePath(pa_o, sys);
                    outObj = sys.io.fetch(null, path);
                    if (outObj == null) {
                        outObj = sys.io.create(null, path, WnRace.FILE);
                    }
                }
            }

            BufferedImage inImg = Images.read(sys.io.getInputStream(inObj, 0));
            BufferedImage outImg = null;
            // 转换图片
            if (scaleZoom) {
                outImg = Images.zoomScale(inImg, sw, sh, bgcolor);
            } else {
                outImg = Images.clipScale(inImg, sw, sh);
            }

            // 写入临时文件
            File tmp = Files.createFileIfNoExists(new File(tmpDir, System.nanoTime()
                                                                   + "."
                                                                   + outObj.type()));
            Images.write(outImg, tmp);
            // 写入outObj中
            OutputStream ops = sys.io.getOutputStream(outObj, 0);
            Streams.writeAndClose(ops, Streams.fileIn(tmp));
            // 清除临时文件
            tmp.delete();
        }
    }
}
