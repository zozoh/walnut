package org.nutz.walnut.web.module;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.segment.Segments;
import org.nutz.lang.util.Context;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.View;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.adaptor.QueryStringAdaptor;
import org.nutz.mvc.annotation.AdaptBy;
import org.nutz.mvc.annotation.At;
import org.nutz.mvc.annotation.By;
import org.nutz.mvc.annotation.Fail;
import org.nutz.mvc.annotation.Filters;
import org.nutz.mvc.annotation.Ok;
import org.nutz.mvc.annotation.POST;
import org.nutz.mvc.annotation.Param;
import org.nutz.mvc.annotation.ReqHeader;
import org.nutz.mvc.view.HttpStatusView;
import org.nutz.mvc.view.ViewWrapper;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.view.WnImageView;
import org.nutz.walnut.web.view.WnObjDownloadView;

/**
 * 提供对于 Obj 的增删改查功能，以便外部程序能够通过 HTTP 访问对象数据
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@Filters(@By(type = WnCheckSession.class))
@At("/o")
@Ok("ajax")
@Fail("ajax")
public class ObjModule extends AbstractWnModule {

    private Map<String, BufferedImage> thumbCache;

    public ObjModule() {
        thumbCache = new Hashtable<String, BufferedImage>();
    }

    @At("/get/**")
    public WnObj get(String str) {
        return Wn.checkObj(io, str);
    }

    @POST
    @At("/fetch")
    public WnObj fetch(@Param("str") String str) {
        return Wn.checkObj(io, Wn.WC().checkSE(), str);
    }

    /**
     * 设置一个对象的元数据
     * 
     * @param str
     *            对象字符串。@see
     *            {@link #checkObj(String, org.nutz.lang.util.NutMap, String)}
     * @param map
     *            都要修改哪些元数据
     * @return 对象
     */
    @At("/set/**")
    @AdaptBy(type = JsonAdaptor.class)
    public WnObj set(String str, NutMap map) {
        WnObj o = Wn.checkObj(io, str);
        io.writeMeta(o, map);
        return o;
    }

    /**
     * 给定查询对象，获取对象列表
     * 
     * @param q
     *            查询对象
     * @return 列表
     */
    @At("/query")
    public List<WnObj> query(@Param("..") WnQuery q) {
        return io.query(q);
    }

    @Inject("java:$conf.get('obj-thumbnail-dft-szhint', 64)")
    private int dftSizeHint;

    /**
     * 返回对象的缩略图
     * 
     * @param str
     *            对象字符串。
     * @param sh
     *            缩略图尺寸提示，可以是 16|24|32|64|128|256，默认来自配置项
     *            "obj-thumbnail-dft-szhint"
     * @param scale
     *            【选】指明缩略图的尺寸，要进行转换，格式必须是 128x128，否则无效
     * 
     * @param f
     *            强制缩放，false 会自动判断，只有尺寸比较大的时候才会缩放，否则变大往往会增加带宽，没啥用
     * 
     * @param bg
     *            【选】缩放的时候,指明缩略图的背景色，如果指定了背景色，那么输出一定是 image/jpeg 格式的 支持的格式
     *            <ul>
     *            <li>RGB: FFF
     *            <li>RRGGBB: F0F0F0
     *            <li>RGB值: rgb(255,33,89)
     *            </ul>
     *            只有在声明强制缩放，或者系统认为有必要缩放的时候，这个参数才会生效
     * 
     * 
     * @return 缩略图的输出流视图
     */
    @At("/thumbnail/**")
    @Ok("void")
    @Fail("http:404")
    public View readThumbnail(String str,
                              @Param("sh") int sizeHint,
                              @Param("scale") String scale,
                              @Param("f") boolean force,
                              @Param("bg") String bg) {
        // 缩略图对象
        BufferedImage im = null;

        // 统一设定默认缩略图的格式
        String mime = "image/png";
        String imtp = "png";

        // 直接指定的就是文件类型
        if (str.startsWith("type:")) {
            im = __read_by_type(sizeHint, null, str.substring("type:".length()));
        }
        // 根据对象找吧
        else {
            // 首先读取对象
            WnObj o = Wn.checkObj(io, str);
            WnRace race = o.race();

            // 如果缩略图，使用
            if (o.hasThumbnail()) {
                WnObj oThumb = Wn.getObj(io, o.thumbnail());
                im = io.readImage(oThumb);
                mime = oThumb.mime();
                imtp = oThumb.type();
            }
            // 否则找默认的，默认的一定是 image/png
            else {
                // 先确定一下类型
                String tp = o.type();
                if (Strings.isBlank(tp)) {
                    tp = o.isDIR() ? "folder" : "unknown";
                }

                im = __read_by_type(sizeHint, race, tp);
            }
        }

        // 是否要缩放
        if (null != im && null != scale) {
            Matcher m = Pattern.compile("^(\\d+)[xX](\\d+)$").matcher(scale);
            if (m.find()) {
                int w = Integer.parseInt(m.group(1));
                int h = Integer.parseInt(m.group(2));

                // 看看有没有必要缩放，如果没声明 force，则比较一下，只要宽高有一个要变小，就值得缩放
                if (force || im.getWidth() > w || im.getHeight() > h) {
                    if (Strings.isBlank(bg)) {
                        im = Images.zoomScale(im, w, h);
                    } else {
                        Color bgcolor = Colors.as(bg);
                        im = Images.zoomScale(im, w, h, bgcolor);
                    }
                }
            }
        }

        // 最后返回 Image 视图
        return new ViewWrapper(new WnImageView(imtp, mime), im);
        // return new ViewWrapper(new RawView(oThumb.mime()), im);
    }

    private BufferedImage __read_by_type(int sizeHint, WnRace race, String tp) {
        BufferedImage im;
        // 读取指定尺寸图片
        sizeHint = sizeHint > 0 ? sizeHint : dftSizeHint;
        // 确保是标准给定尺寸
        int s_h = -1;
        for (int i = 0; i < SIZE_HINTS.length; i++) {
            if (SIZE_HINTS[i] >= sizeHint) {
                s_h = SIZE_HINTS[i];
                break;
            }
        }
        sizeHint = s_h < 0 ? SIZE_HINTS[SIZE_HINTS.length - 1] : s_h;
        String sz_key = String.format("%1$dx%1$d", sizeHint);

        // 尝试从本域读取默认缩略图
        String ph = Wn.normalizeFullPath("~/.thumbnail/dft/"
                                         + tp
                                         + "/"
                                         + sz_key
                                         + ".png",
                                         Wn.WC().checkSE());
        WnObj oThumb = io.fetch(null, ph);

        // 如果找到了 ...
        if (null != oThumb) {
            im = io.readImage(oThumb);
        }
        // 没找到就用系统的缩略图，这样可以利用上缓存
        else {
            im = __read_dft_thumbnail(tp, race, sz_key);
        }
        return im;
    }

    private static final int[] SIZE_HINTS = Nums.array(16, 24, 32, 64, 128);

    private BufferedImage __read_dft_thumbnail(String tp, WnRace race, String sz_key) {
        BufferedImage im;

        // 先看看缓存里有木有
        String thumb_key = tp + "_" + sz_key;
        im = thumbCache.get(thumb_key);
        if (null != im)
            return im;

        // 从系统的缩略图目录里找
        WnObj oThumbHome = io.fetch(null, "/etc/thumbnail/" + tp);

        // 如果是目录，没找到的话，试图读取 "folder" 作为缩略图
        if (null == oThumbHome && race == WnRace.DIR) {
            oThumbHome = io.fetch(null, "/etc/thumbnail/folder");
        }

        // 没找到了对应文件的缩略图，直接用默认的
        if (null == oThumbHome) {
            return __read_unknown_thumbnail(sz_key);
        }

        // 如果就找的其实是个文件夹，那么根据尺寸来找，没给尺寸的话
        WnObj oThumb = io.fetch(oThumbHome, sz_key + ".png");

        // 还是没有，用默认图片
        if (null == oThumb) {
            return __read_unknown_thumbnail(sz_key);
        }

        // 那么读取一下图片，并存入缓存
        im = io.readImage(oThumb);
        thumbCache.put(thumb_key, im);

        // 返回
        return im;
    }

    private BufferedImage __read_unknown_thumbnail(String sz_key) {
        BufferedImage im;
        String thumb_key = "unknown_" + sz_key;
        im = thumbCache.get(thumb_key);
        if (null == im) {
            WnObj oThumb = io.check(null, "/etc/thumbnail/unknown/" + sz_key + ".png");
            im = io.readImage(oThumb);
            thumbCache.put("unknown_" + sz_key, im);
        }
        return im;
    }

    /**
     * 得到对象内容
     * 
     * @param str
     *            对象字符串。
     * 
     * @param sha1
     *            SHA1 校验，可选。如果给定了值，如果对象的内容不符合这个指纹，则抛错
     * 
     * @return 对象当前的内容输入流
     * 
     * @see org.nutz.walnut.util.Wn#checkObj(org.nutz.walnut.api.io.WnIo,
     *      String)
     */
    @At("/read/**")
    @Ok("void")
    public View read(String str, @Param("sha1") String sha1, @ReqHeader("User-Agent") String ua) {
        // 首先得到目标对象
        WnObj o = Wn.checkObj(io, str);

        // 校验 sha1
        if (!Strings.isBlank(sha1)) {
            if (!o.isSameSha1(sha1))
                return new HttpStatusView(400);
        }

        // 读取对象的值
        return new WnObjDownloadView(io, o, ua);
    }

    @At("/write/**")
    public WnObj write(String str, InputStream ins) {
        WnObj o = Wn.checkObj(io, str);
        OutputStream ops = io.getOutputStream(o, 0);
        Streams.writeAndClose(ops, ins);
        return o;
    }

    /**
     * 从本地上传一个文件流
     * 
     * @param str
     *            对象字符串。@see
     *            {@link #checkObj(String, org.nutz.lang.util.NutMap, String)}
     * @param nm
     *            本地文件名
     * @param sz
     *            本地文件尺寸
     * @param mime
     *            本地文件的内容类型
     * @param dupp
     *            如果目录下有重名，应该用什么样的文件名格式生成新文件。 <br>
     *            默认为 null 则表示，遇到重名的就覆盖<br>
     *            参数的值的意义为:
     * 
     *            <pre>
     *            # 参数就是一个字符串模板，比如
     *            "${major}(${nb})${suffix}"
     *            
     *            - 里面一定有三个固定的占位符 major, nb, suffix
     *            - 比如 abc.png 那么，它的 major 是 "abc" 它的 suffix 是 ".png"
     *            - 根据这个字符串模板，函数会从 nb=1 开始试验有没有重名，直到发现不重名的对象为止
     *            - 对于 abc.png ，对于上面那个模板重试的顺序就是
     *            -- abc(1).png
     *            -- abc(2).png
     *            -- abc(3).png
     *            -- ..
     *            - 总之根据这个字符串模板，你可以定制自己的重命名方式
     * 
     *            </pre>
     * 
     * @param objnm
     *            指定了上传的对象名称，如果这个对象不存在，创建它。<br>
     *            如果 str 指定的上传目标是个文件，且名称与 objnm 不同<br>
     *            则在同目录下再创建一个文件
     * 
     * @param ins
     *            文件内容流
     * @return 创建的对象
     */
    @At("/upload/**")
    @AdaptBy(type = QueryStringAdaptor.class)
    public WnObj upload(String str,
                        @Param("abpath") boolean abpath,
                        @Param("nm") String nm,
                        @Param("cie") boolean createIfNoExists,
                        @Param("race") WnRace race,
                        @Param("sz") long sz,
                        @Param("mime") String mime,
                        @Param("dupp") String dupp,
                        InputStream ins) {
        // 首先得到目标对象
        WnObj ta;
        if (createIfNoExists && !str.startsWith("id:")) {
            // 绝对目录 会丢失第一个字符 /
            if (abpath && !str.startsWith("/")) {
                str = "/" + str;
            }
            String ph = Wn.normalizeFullPath(str, Wn.WC().checkSE());
            ta = io.createIfNoExists(null, ph, race);
        } else {
            String id = str.substring("id:".length());
            ta = io.checkById(id);
        }

        WnObj o;
        // 如果目标对象是个文件
        if (ta.isFILE()) {
            o = ta;
        }
        // 如果是个目录，则试图创建一个新文件
        else if (ta.isDIR()) {
            String fname = nm;
            // 如果重名就覆盖的话 ...
            if (Strings.isBlank(dupp)) {
                o = io.createIfNoExists(ta, fname, WnRace.FILE);
            }
            // 那么重名的话，则创建新文件
            else {
                if (io.exists(ta, fname)) {
                    Context c = Lang.context();
                    c.set("major", Files.getMajorName(nm));
                    c.set("suffix", Files.getSuffix(nm));
                    Segment seg = Segments.create(dupp);
                    int i = 1;
                    do {
                        c.set("nb", i++);
                        fname = seg.render(c).toString();
                    } while (io.exists(ta, fname));
                }
                // 创建文件对象
                o = io.create(ta, fname, WnRace.FILE);
            }
        }
        // 否则则抛错
        else {
            throw Er.create("e.api.o.upload.invalid_target", ta);
        }

        // 写入
        OutputStream ops = io.getOutputStream(o, 0);
        Streams.writeAndClose(ops, ins);

        // 返回
        return o;
    }
}
