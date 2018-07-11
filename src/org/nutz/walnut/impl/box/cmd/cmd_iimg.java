package org.nutz.walnut.impl.box.cmd;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;
import org.nutz.trans.Atom;
import org.nutz.trans.Proton;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_iimg extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "Qcnqlf", "^(force|compress)$");

        // 读取文件
        if (params.vals.length == 0)
            throw Er.create("e.cmd.iimg.noinput");

        WnObj oim = Wn.checkObj(sys, params.vals[0]);

        // 不是图片抛错
        if (!oim.mime().startsWith("image/") && !params.is("force"))
            throw Er.create("e.cmd.iimg.noimage", oim);

        // 看看系统里面是否已经有了这个图片的缩略图
        WnObj o_old = null;
        if (!params.is("force")) {
            WnQuery q = new WnQuery();
            q.setv("mime", oim.mime());
            q.setv("sha1", oim.sha1());
            q.setv("thumb", "^id:.+");
            q.setv("width", "[1,]");
            q.setv("height", "[1,]");
            o_old = sys.nosecurity(new Proton<WnObj>() {
                @Override
                protected WnObj exec() {
                    return sys.io.getOne(q);
                }
            });
        }

        // 重新计算
        if (null == o_old) {
            __force_gen(sys, params, oim);
        }
        // 利用现成结果
        else {
            // 看看要不要更新图片文件的元数据
            int im_w = o_old.getInt("width");
            int im_h = o_old.getInt("height");
            if (oim.getInt("width") != im_w || oim.getInt("height") != im_h) {
                oim.setv("width", im_w).setv("height", im_h);
                sys.io.set(oim, "^(width|height)$");
            }

            // 要不要生成缩略图
            if (params.has("thumb")) {
                final WnObj o_old2 = o_old;
                WnObj o_old_thumb = sys.nosecurity(new Proton<WnObj>() {
                    @Override
                    protected WnObj exec() {
                        return Wn.getObj(sys, o_old2.thumbnail());
                    }
                });
                _gen_thumb(sys, params, oim, null, o_old_thumb);
            }
        }

        if (params.has("clip")) {
            String cpoints = params.getString("clip");
            String[] parr = Strings.splitIgnoreBlank(cpoints, ",");
            if (parr.length == 4) {
                int[] startPoint = new int[]{Integer.parseInt(parr[0]), Integer.parseInt(parr[1])};
                int[] endPoint = new int[]{startPoint[0]
                                           + Integer.parseInt(parr[2]),
                                           startPoint[1] + Integer.parseInt(parr[3])};
                BufferedImage im = sys.io.readImage(oim);
                BufferedImage im2 = Images.clipScale(im, startPoint, endPoint);
                WnObj outObj = oim;
                if (params.has("out")) {
                    String outPath = Wn.normalizeFullPath(params.get("out"), sys);
                    WnObj onew = sys.io.fetch(null, outPath);
                    if (onew != null) {
                        outObj = onew;
                    } else {
                        outObj = sys.io.createIfNoExists(null, outPath, WnRace.FILE);
                    }
                }
                sys.io.writeImage(outObj, Images.redraw(im2, Color.white));
            } else {
                sys.err.print("err clip params, must be num like 0,0,100,100");
            }
        }
        // 原地压缩
        if (params.is("compress")) {
            // 是否指定了jpeg质量
            double quality = params.getDouble("quality", 0.8f);
            int w = params.getInt("width", -1);
            int h = params.getInt("height", -1);
            BufferedImage srcImg = sys.io.readImage(oim);
            if (w > 0) {
                h = (int)(w / (srcImg.getWidth() + 0.0) * srcImg.getHeight());
            }
            else if (h > 0) {
                w = (int)(h / (srcImg.getHeight() + 0.0) * srcImg.getWidth());
            }
            BufferedImage dstImg = srcImg;
            if (w > 0 && h > 0)
                dstImg = Images.zoomScale(srcImg, w, h);
            WnObj dsto = oim;
            if (params.has("dst"))
                dsto = sys.io.createIfNoExists(null, Wn.normalizeFullPath(params.get("dst"), sys), WnRace.FILE);
            try (OutputStream out = sys.io.getOutputStream(dsto, 0)) {
                Images.writeJpeg(dstImg, out, (float)quality);
            }
        }

        // 最后输出
        if (!params.is("Q")) {
            JsonFormat fmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(oim, fmt));
        }

    }

    private void __force_gen(WnSystem sys, ZParams params, WnObj oim) {
        // 读取图片
        BufferedImage im = sys.io.readImage(oim);
        if (im == null) {
            return;
        }

        // 看看要不要更新图片文件的元数据
        int im_w = im.getWidth();
        int im_h = im.getHeight();
        if (oim.getInt("width") != im_w || oim.getInt("height") != im_h) {
            oim.setv("width", im_w).setv("height", im_h);
            sys.io.set(oim, "^(width|height)$");
        }

        // 要不要生成缩略图
        if (params.has("thumb")) {
            _gen_thumb(sys, params, oim, im, null);
        }
    }

    private void _gen_thumb(WnSystem sys,
                            ZParams params,
                            WnObj oim,
                            BufferedImage im,
                            WnObj o_old_thumb) {
        // 当然，如果是 thumbnail 里面的图片 ...
        String aph = Wn.normalizeFullPath("~/.thumbnail/gen/", sys);
        WnObj oThumbHome = sys.io.createIfNoExists(null, aph, WnRace.DIR);

        // 就不要再生成缩略图了
        if (oim.isMyParent(oThumbHome)) {
            return;
        }

        Pattern p = Regex.getPattern("^(\\d+)[xX](\\d+)$");
        Matcher m = p.matcher(params.check("thumb"));
        if (m.find()) {
            int w = Integer.parseInt(m.group(1));
            int h = Integer.parseInt(m.group(2));

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

            // 如果尺寸匹配，就不再生成了，直接使用
            if (null != o_old_thumb
                && w == o_old_thumb.getInt("width")
                && h == o_old_thumb.getInt("height")) {
                if (!o_old_thumb.isSameId(oThumb)) {
                    WnObj oThumb2 = oThumb;
                    sys.nosecurity(new Atom() {
                        @Override
                        public void run() {
                            sys.io.copyData(o_old_thumb, oThumb2);
                        }
                    });
                }
            }
            // 否则是要生成的
            else {
                // 确保读取了图片
                if (null == im) {
                    im = sys.io.readImage(oim);
                }

                // 开始转换
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
                    String cpoints = params.getString("clip");
                    if (Strings.isBlank(cpoints)) {
                        im2 = Images.clipScale(im, w, h);
                    } else {
                        String[] parr = Strings.splitIgnoreBlank(cpoints, ",");
                        int[] startPoint = new int[]{Integer.parseInt(parr[0]),
                                                     Integer.parseInt(parr[1])};
                        int[] endPoint = new int[]{startPoint[0]
                                                   + Integer.parseInt(parr[2]),
                                                   startPoint[1] + Integer.parseInt(parr[3])};
                        im2 = Images.clipScale(im, startPoint, endPoint);
                    }
                }

                // 最后写入
                sys.io.writeImage(oThumb, Images.redraw(im2, Color.BLACK));
            }
        }
    }

}
