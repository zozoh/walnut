package org.nutz.walnut.impl.box.cmd;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_iimg extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "Qcnql");

        // 读取文件
        if (params.vals.length == 0)
            throw Er.create("e.cmd.iimg.noinput");

        WnObj oim = Wn.checkObj(sys, params.vals[0]);

        // 不是图片抛错
        if (!oim.mime().startsWith("image/"))
            throw Er.create("e.cmd.iimg.noimage", oim);

        // 读取图片
        BufferedImage im = sys.io.readImage(oim);

        // 看看要不要更新图片文件的元数据
        int im_w = im.getWidth();
        int im_h = im.getHeight();
        if (oim.getInt("width") != im_w || oim.getInt("height") != im_h) {
            oim.setv("width", im_w).setv("height", im_h);
            sys.io.set(oim, "^(width|height)$");
        }

        // 要不要生成缩略图
        if (params.has("thumb")) {
            _gen_thumb(sys, params, oim, im);
        }

        // 最后输出
        if (!params.is("Q")) {
            JsonFormat fmt = this.gen_json_format(params);
            sys.out.println(Json.toJson(oim, fmt));
        }

    }

    private void _gen_thumb(WnSystem sys, ZParams params, WnObj oim, BufferedImage im) {
        // 当然，如果是 thumbnail 里面的图片 ...
        String aph = Wn.normalizeFullPath("~/.thumbnail/gen/", sys);
        WnObj oThumbHome = sys.io.createIfNoExists(null, aph, WnRace.DIR);

        // 就不要再生成缩略图了
        if (oim.isMyParent(oThumbHome)) {
            return;
        }

        Pattern p = Pattern.compile("^(\\d+)[xX](\\d+)$");
        Matcher m = p.matcher(params.check("thumb"));
        if (m.find()) {
            int w = Integer.parseInt(m.group(1));
            int h = Integer.parseInt(m.group(2));
            BufferedImage im2 = null;
            // 缩放
            if ("zoom".equals(params.get("mode", "zoom"))) {
                Color bgColor = null;
                if (params.has("bgc"))
                    bgColor = Colors.as(params.get("bgc"));

                im2 = Images.zoomScale(im, w, h, bgColor);
            }
            // 剪裁
            else {
                im2 = Images.clipScale(im, w, h);
            }

            // 那么得到目标缩略图
            WnObj oThumb = null;
            if (oim.hasThumbnail()) {
                oThumb = Wn.getObj(sys, oim.thumbnail());
            }

            // 创建缩略图，都是 (JPEG)
            if (null == oThumb) {
                oThumb = sys.io.createIfNoExists(oThumbHome, oim.id() + ".jpg", WnRace.FILE);
                oThumb.setv("thumb_src", "id:" + oim.id());
                sys.io.set(oThumb, "^thumb_src$");

                oim.thumbnail("id:" + oThumb.id());
                sys.io.set(oim, "^thumb$");
            }

            // 缩略图的宽高元数据
            if (oThumb.getInt("width") != w || oThumb.getInt("height") != h) {
                oThumb.setv("width", w).setv("height", h);
                sys.io.set(oThumb, "^(width|height)$");
            }

            // 最后写入
            sys.io.writeImage(oThumb, im2);
        }
    }

}
