package org.nutz.walnut.impl.box.cmd;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.img.Images;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.random.R;
import org.nutz.lang.util.Regex;
import org.nutz.log.Log;
import org.nutz.log.Logs;
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

import net.coobird.thumbnailator.Thumbnails;

public class cmd_iimg extends JvmExecutor {

    private static final Log log = Logs.get();

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {

        ZParams params = ZParams.parse(args, "Qcnqlf", "^(force|compress|native)$");

        // 读取文件
        String imPh = params.val_check(0);
        WnObj oim = Wn.checkObj(sys, imPh);

        // 如果这个图片是空的，那就啥也不干
        if (Strings.isBlank(oim.sha1())) {
            return;
        }
        // 如果不是图片，也不行
        if (!oim.hasMime() || !oim.isMime("^image/.+")) {
            return;
        }

        // 如果是生成缩略图，那么是否指定了特殊的缩略图生成目标
        String thumbTa = params.get("thumbta");
        WnObj oThumbTa = oim;
        if (!Strings.isBlank(thumbTa) && "true".equals(thumbTa)) {
            oThumbTa = sys.io.fetch(oim, thumbTa);
        }

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
            __force_gen(sys, params, oim, oThumbTa);
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
                _gen_thumb(sys, params, oim, null, o_old_thumb, oThumbTa);
            }
        }

        if (params.has("clip")) {
            String cpoints = params.getString("clip");
            String[] parr = Strings.splitIgnoreBlank(cpoints, ",");
            if (parr.length == 4) {
                int[] startPoint = new int[]{Integer.parseInt(parr[0]), Integer.parseInt(parr[1])};
                int[] endPoint = new int[]{startPoint[0] + Integer.parseInt(parr[2]),
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
            double quality = params.getDouble("quality", 0.9f);
            WnObj dsto = oim;
            if (params.has("dst"))
                dsto = sys.io.createIfNoExists(null,
                                               Wn.normalizeFullPath(params.get("dst"), sys),
                                               WnRace.FILE);
            if (params.is("native") && new File("/usr/bin/convert").exists()) {
                String tmpName = R.UU32() + ".jpg";
                File f = new File("/dev/shm/" + tmpName);
                File tmp = new File("/dev/shm/new_" + tmpName);
                log.info("native convert :" + oim.path());
                try (OutputStream out = new FileOutputStream(f)) {
                    sys.io.readAndClose(oim, out);
                }
                Lang.execOutput(new String[]{"/usr/bin/convert",
                                             "-auto-orient",
                                             "-strip",
                                             "-quality",
                                             "80%",
                                             "-resize",
                                             params.get("resize", "1920x1920"),
                                             f.getAbsolutePath(),
                                             tmp.getAbsolutePath()});
                if (tmp.exists() && tmp.length() > 100 * 1024 && tmp.length() < f.length()) {
                    try (InputStream ins = new FileInputStream(tmp)) {
                        sys.io.writeAndClose(dsto, ins);
                    }
                }
                f.delete();
                tmp.delete();
            } else {
                int w = params.getInt("width", -1);
                int h = params.getInt("height", -1);
                BufferedImage srcImg = sys.io.readImage(oim);
                if (w > 0) {
                    h = (int) (w / (srcImg.getWidth() + 0.0) * srcImg.getHeight());
                } else if (h > 0) {
                    w = (int) (h / (srcImg.getHeight() + 0.0) * srcImg.getWidth());
                }
                BufferedImage dstImg = srcImg;
                if (w > 0 && h > 0)
                    dstImg = Images.zoomScale(srcImg, w, h);

                try (OutputStream out = sys.io.getOutputStream(dsto, 0)) {
                    Images.writeJpeg(dstImg, out, (float) quality);
                }
            }
        }

        // 最后输出
        if (!params.is("Q")) {
            JsonFormat fmt = Cmds.gen_json_format(params);
            sys.out.println(Json.toJson(oim, fmt));
        }

    }

    private void __force_gen(WnSystem sys, ZParams params, WnObj oim, WnObj oThumbTa)
            throws IOException {
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
            _gen_thumb(sys, params, oim, im, null, oThumbTa);
        }
    }

    private void _gen_thumb(WnSystem sys,
                            ZParams params,
                            WnObj oim,
                            BufferedImage im,
                            WnObj o_old_thumb,
                            WnObj oThumbTa)
            throws IOException {
        // 当然，如果是 thumbnail 里面的图片 ...
        // 为了确保缩略图都能被读到，所以要放到 oim.d1 所在的路径下
        String aph = "/home/" + oim.d1() + "/.thumbnail/gen/";
        WnObj oThumbHome = sys.io.createIfNoExists(null, aph, WnRace.DIR);

        // 缩略图目录里的就不要再生成缩略图了，否则会递归的吧...
        if (null == oThumbTa || oThumbTa.isMyParent(oThumbHome)) {
            return;
        }

        Pattern p = Regex.getPattern("^(\\d+)[xX](\\d+)$");
        Matcher m = p.matcher(params.check("thumb"));
        // 嗯，有缩略图属性，且写法是合法的
        if (m.find()) {
            int w = Integer.parseInt(m.group(1));
            int h = Integer.parseInt(m.group(2));

            // 那么得到目标缩略图
            WnObj oThumb = null;
            if (oThumbTa.hasThumbnail()) {
                oThumb = Wn.getObj(sys, oThumbTa.thumbnail());
            }

            // 创建缩略图，都是 (JPEG)
            if (null == oThumb) {
                oThumb = sys.io.createIfNoExists(oThumbHome, oim.id() + ".jpg", WnRace.FILE);
                oThumb.setv("thumb_src", "id:" + oThumbTa.id());
                oThumb.group(oim.group()).creator(oim.creator());
                sys.io.set(oThumb, "^(c|g|thumb_src)$");

                oThumbTa.thumbnail("id:" + oThumb.id());
                sys.io.set(oThumbTa, "^thumb$");
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
                    // Color bgColor = null;
                    // if (params.has("bgc"))
                    // bgColor = Colors.as(params.get("bgc"));
                    //
                    // im2 = Images.zoomScale(im, w, h, bgColor);
                    Thumbnails.Builder<BufferedImage> holder = Thumbnails.of(im);
                    double quality = params.getDouble("quality", .9d);
                    holder.outputQuality(quality);
                    holder.outputFormat("JPEG");
                    holder.size(w, h);
                    im2 = holder.asBufferedImage();

                    // File ddir = Files.findFile("D:\\Download");
                    // File f1 = Files.getFile(ddir, "bb.jpg");
                    // File f2 = Files.getFile(ddir, "ss.jpg");
                    // Files.createFileIfNoExists(f1);
                    // Files.createFileIfNoExists(f2);
                    //
                    // Images.write(im, f1);
                    // Images.write(im2, f2);

                    OutputStream ops = sys.io.getOutputStream(oThumb, 0);
                    try {
                        holder.toOutputStream(ops);
                    }
                    finally {
                        Streams.safeFlush(ops);
                        Streams.safeClose(ops);
                    }
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
                        int[] endPoint = new int[]{startPoint[0] + Integer.parseInt(parr[2]),
                                                   startPoint[1] + Integer.parseInt(parr[3])};
                        im2 = Images.clipScale(im, startPoint, endPoint);
                    }
                    sys.io.writeImage(oThumb, im2);
                }
            }
        }
    }

}
