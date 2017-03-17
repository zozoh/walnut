package org.nutz.walnut.impl.box.cmd;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
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

        ZParams params = ZParams.parse(args, "Qcnqlf", "^(force)$");

        // 读取文件
        if (params.vals.length == 0)
            throw Er.create("e.cmd.iimg.noinput");

        WnObj oim = Wn.checkObj(sys, params.vals[0]);

        // 不是图片抛错
        if (!oim.mime().startsWith("image/"))
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
                        return Wn.checkObj(sys, o_old2.thumbnail());
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

        // 最后输出
        if (!params.is("Q")) {
            JsonFormat fmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(oim, fmt));
        }

    }

    private void __force_gen(WnSystem sys, ZParams params, WnObj oim) {
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

        Pattern p = Pattern.compile("^(\\d+)[xX](\\d+)$");
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
