package org.nutz.walnut.ext.imagic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.imagic.filter.AutoExifImagicFilter;
import org.nutz.walnut.ext.imagic.filter.ClipImagicFilter;
import org.nutz.walnut.ext.imagic.filter.ContainsImagicFilter;
import org.nutz.walnut.ext.imagic.filter.CoverImagicFilter;
import org.nutz.walnut.ext.imagic.filter.RotateImagicFilter;
import org.nutz.walnut.ext.imagic.filter.ScaleImagicFilter;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifSubIFDDirectory;

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
        ZParams params = ZParams.parse(args, null);
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
        if (params.has("format")) {
            holder.outputFormat(params.get("format"));
        } else if (params.has("out")) {
            if (params.get("out").endsWith(".png")) {
                holder.outputFormat(params.get("PNG"));
            } else if (params.get("out").endsWith(".bmp")) {
                holder.outputFormat(params.get("BMP"));
            } else {
                holder.outputFormat("JPEG");
            }
        } else {
            holder.outputFormat("JPEG");
        }
        // 输出质量有要求吗?
        if (params.has("qa")) {
            holder.outputQuality(Float.parseFloat(params.get("qa")));
        }
        // 输出到哪里呢?
        OutputStream outs = null;
        try {
            if (params.has("out")) {
                // 有输出路径
                String out = params.get("out");
                if ("inplace".equals(out)) {
                    out = sourcePath;
                } else {
                    out = Wn.normalizeFullPath(out, sys);
                }
                WnObj wobjOut = sys.io.createIfNoExists(null, out, WnRace.FILE);
                outs = sys.io.getOutputStream(wobjOut, 0);
            } else {
                outs = sys.out.getOutputStream();
            }
            holder.toOutputStream(outs);
        }
        finally {
            Streams.safeClose(outs);
        }
    }
    
    protected BufferedImage readStream(InputStream ins) throws Exception {
        byte[] buf = Streams.readBytesAndClose(ins);
        Metadata meta = ImageMetadataReader.readMetadata(new ByteArrayInputStream(buf));
        int route = 0;
        if (meta != null) {
            for (Directory directory : meta.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    if (tag.getTagType() == ExifSubIFDDirectory.TAG_ORIENTATION) {
                        String value = tag.getDescription();
                        if (value.contains("Right side, top")) {
                            route = 90;
                        }
                        else if (value.contains("Left side, top")) {
                            route = -90;
                        }
                        else if (value.contains("Bottom side, top")) {
                            route = 180;
                        }
                    }
                }
            }
        }
        return Thumbnails.of(new ByteArrayInputStream(buf)).useExifOrientation(false).rotate(route).scale(1.0).asBufferedImage();
    }
}
