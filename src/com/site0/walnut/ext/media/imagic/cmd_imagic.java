package com.site0.walnut.ext.media.imagic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.media.imagic.filter.AutoExifImagicFilter;
import com.site0.walnut.ext.media.imagic.filter.ClipImagicFilter;
import com.site0.walnut.ext.media.imagic.filter.ContainsImagicFilter;
import com.site0.walnut.ext.media.imagic.filter.CoverImagicFilter;
import com.site0.walnut.ext.media.imagic.filter.RotateImagicFilter;
import com.site0.walnut.ext.media.imagic.filter.ScaleImagicFilter;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wpath;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectoryBase;
import net.coobird.thumbnailator.Thumbnails;

public class cmd_imagic extends JvmExecutor {

    protected Map<String, ImagicFilter> filters = new HashMap<>();

    public cmd_imagic() {
        // 任意角度旋转
        filters.put("rotate", new RotateImagicFilter());
        // 裁剪
        filters.put("clip", new ClipImagicFilter());
        // 等比缩放
        filters.put("scale", new ScaleImagicFilter());
        // 根据EXIF信息自动旋转,通常用于禁用,因为陌生是根据EXIF旋转的
        filters.put("autoexif", new AutoExifImagicFilter());
        // 放入指定大小
        filters.put("contains", new ContainsImagicFilter());
        // 填满指定大小
        filters.put("cover", new CoverImagicFilter());
    }

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 解析一下参数
        ZParams params = ZParams.parse(args, "cqn", "^(quiet|stream)$");
        // 解析源图片
        BufferedImage image = null;
        String sourcePath = null;
        if (params.vals.length > 0) {
            String tmp = params.val(0);
            if (tmp.startsWith("http://") || tmp.startsWith("https://")) {
                image = readStream(new URL(tmp).openStream());
            } else {
                sourcePath = Wn.normalizeFullPath(tmp, sys);
                WnObj wobj = sys.io.check(null, sourcePath);
                try (InputStream ins = sys.io.getInputStream(wobj, 0)) {
                    image = readStream(ins);
                }
            }
        } else if (sys.pipeId > 0) {
            image = readStream(sys.in.getInputStream());
        } else {
            sys.err.print("e.cmd.imagic.need_image_path");
            return;
        }

        // 是不是有过滤器配置呢?
        if (params.has("filter")) {
            // 构建过滤链

            // "abc(1,2) bbb() zzz(false)"
            String[] tmp2 = Strings.splitIgnoreBlank(params.get("filter"), "\\) ");
            for (String tmp3 : tmp2) {
                // ["abc(1,2", "bbb(", "zzz(false)"]
                if (tmp3.endsWith(")")) {
                    tmp3 = tmp3.substring(0, tmp3.length() - 1);
                }
                // 获取过滤器的名字
                String name = tmp3.substring(0, tmp3.indexOf('('));
                ImagicFilter cFilter = this.filters.get(name);
                if (cFilter == null) {
                    sys.err.print("e.cmd.imagic.no_such_filter." + name);
                    return;
                }
                // 获取过滤器的参数
                String filterArgs = null;
                if (tmp3.endsWith("(")) {
                    filterArgs = "";
                } else {
                    filterArgs = tmp3.substring(tmp3.indexOf('(') + 1).trim();
                }

                // 执行过滤器
                image = cFilter.doChain(image, filterArgs);
            }
        }

        // 创建图片对象
        Thumbnails.Builder<BufferedImage> holder = Thumbnails.of(image);
        holder.size(image.getWidth(), image.getHeight());

        // 输出格式是啥呢?
        String out = params.getString("out");
        String ofmt = "JPEG";
        if (params.has("format")) {
            ofmt = params.getString("format").toUpperCase();
        }
        // 通过 output 指定
        else if (Ws.isBlank(out)) {
            if ("inplace".equals(out)) {
                ofmt = Wpath.getSuffixName(sourcePath);
            } else {
                ofmt = Wpath.getSuffixName(out);
            }
        }
        //
        // 格式化一下标准的 format 名称
        //
        if ("JPG".equals(ofmt)) {
            ofmt = "JPEG";
        }
        holder.outputFormat(ofmt);

        // 输出质量有要求吗?
        if (params.has("qa")) {
            holder.outputQuality(Float.parseFloat(params.get("qa")));
        }
        // 输出到哪里呢?
        OutputStream outs = null;
        WnObj oOut = null;
        try {
            if (params.has("out")) {
                // 有输出路径
                if ("~self~".equals(out)) {
                    out = sourcePath;
                } else {
                    out = Wn.normalizeFullPath(out, sys);
                }
                oOut = sys.io.createIfNoExists(null, out, WnRace.FILE);
                outs = sys.io.getOutputStream(oOut, 0);
                if (!params.is("stream")) {
                    NutMap meta = new NutMap();
                    meta.put("width", image.getWidth());
                    meta.put("height", image.getHeight());
                    meta.put("mime", "image/" + ofmt.toLowerCase());
                    sys.io.appendMeta(oOut, meta);
                }
            } else {
                outs = sys.out.getOutputStream();
            }
            holder.toOutputStream(outs);
        }
        finally {
            Streams.safeClose(outs);
        }

        // 最后输出
        if (null != oOut && !params.is("quiet")) {
            JsonFormat jfmt = Cmds.gen_json_format(params);
            String str = Json.toJson(oOut, jfmt);
            sys.out.println(str);
        }
    }

    protected BufferedImage readStream(InputStream ins) throws Exception {
        byte[] buf = Streams.readBytesAndClose(ins);
        Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(buf));
        int route = 0;
        if (metadata != null) {
            Directory directory = metadata.getFirstDirectoryOfType(ExifDirectoryBase.class);
            int orientation = 0;
            if (directory != null) {
                if (directory != null && directory.containsTag(ExifDirectoryBase.TAG_ORIENTATION)) {
                    orientation = directory.getInt(ExifDirectoryBase.TAG_ORIENTATION);
                }
                if (6 == orientation) {
                    // 6旋转90
                    route = 90;
                } else if (3 == orientation) {
                    // 3旋转180
                    route = 180;
                } else if (8 == orientation) {
                    // 8旋转90
                    route = 270;
                }
            }
        }
        return Thumbnails.of(new ByteArrayInputStream(buf))
                         .useExifOrientation(false)
                         .rotate(route)
                         .scale(1.0)
                         .asBufferedImage();
    }
}
