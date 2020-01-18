package org.nutz.walnut.web.module;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.nutz.img.Colors;
import org.nutz.img.Images;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Nums;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
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
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnPagerObj;
import org.nutz.walnut.web.filter.WnCheckSession;
import org.nutz.walnut.web.filter.WnSetSecurity;
import org.nutz.walnut.web.util.WnWeb;
import org.nutz.walnut.web.view.WnImageView;
import org.nutz.walnut.web.view.WnObjDownloadView;

/**
 * 提供对于 Obj 的增删改查功能，以便外部程序能够通过 HTTP 访问对象数据
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@IocBean
@Filters({@By(type = WnCheckSession.class), @By(type = WnSetSecurity.class)})
@At("/o")
@Ok("ajax")
@Fail("ajax")
public class ObjModule extends AbstractWnModule {

    private Map<String, BufferedImage> thumbCache;

    public ObjModule() {
        thumbCache = new Hashtable<String, BufferedImage>();
    }

    @Deprecated
    @At("/get/**")
    public WnObj get(String str, @Param("aph") boolean isAbsolutePath) {
        str = __format_str(str, isAbsolutePath);
        return Wn.checkObj(io, str);
    }

    @At("/fetch")
    public WnObj fetch(@Param("str") String str) {
        WnAuthSession se = Wn.WC().checkSession();
        return Wn.checkObj(io, se, str);
    }

    /**
     * 获取某个对象的祖先列表。从顶级目录开始一直到给定对象的父，相当于执行命令:
     * 
     * <code>
     * obj [str] -an
     * </code>
     * 
     * @param str
     *            对象字符串。@see {@link Wn#checkObj}
     * @return 对象的祖先列表（通常第0位为 <code>/home</code>）
     */
    @At("/ancestors")
    public List<WnObj> ancestors(@Param("str") String str) {
        WnAuthSession se = Wn.WC().checkSession();
        WnObj meta = Wn.checkObj(io, se, str);
        LinkedList<WnObj> ans = new LinkedList<>();
        meta.loadParents(ans, false);
        return ans;
    }

    /**
     * @deprecated 不赞成采用本方法，比较危险，将在未来的某个版本（看心情）删除本方法。<br>
     *             请用 <code>update</code> 方法代替
     * 
     *             重设一个对象的元数据
     * 
     * @param str
     *            对象字符串。@see {@link Wn#checkObj}
     * @param map
     *            都要修改哪些元数据
     * @return 对象
     * @see #update(String, boolean, NutMap)
     */
    @Deprecated
    @At("/set/**")
    @AdaptBy(type = JsonAdaptor.class)
    public WnObj set(String str, @Param("aph") boolean isAbsolutePath, NutMap map) {
        // 确保 str 的形式正确
        str = __format_str(str, isAbsolutePath);

        // 取得并写入
        WnObj o = Wn.checkObj(io, str);
        // io.writeMeta(o, map);
        io.appendMeta(o, map);
        return o;
    }

    /**
     * 更新一个对象的元数据
     * 
     * @param str
     *            对象字符串。@see {@link Wn#checkObj}
     * @param map
     *            都要修改哪些元数据
     * @return 对象
     */
    @At("/update")
    @AdaptBy(type = JsonAdaptor.class)
    public WnObj update(@Param("str") String str, NutMap map) {
        // 确保 str 的形式正确
        WnAuthSession se = Wn.WC().checkSession();

        // 取得并写入
        WnObj o = Wn.checkObj(io, se, str);
        io.appendMeta(o, map);
        return o;
    }

    /**
     * 给定查询对象，获取对象列表
     * 
     * @param q
     *            查询对象，如果不指定，默认限制为 100，如果指定最大可查询的记录为 5000
     * @return
     * 
     *         如果 <code>q.limit > 0</code> 返回分页信息
     * 
     *         <pre>
     * {
     *  list:[..], 
     *  pager: {
     *      pageNumber,
     *      pageSize,
     *      pageCount,
     *      totalCount
     *  }
     * }
     *         </pre>
     * 
     *         否则直接返回返回数组
     * 
     *         <pre>
     * [..]
     *         </pre>
     */
    @At("/query")
    public Object query(@Param("..") WnQuery q) {
        boolean isPaging = q.limit() > 0;

        // 确保数据的限制
        if (q.limit() > 5000) {
            q.limit(5000);
        } else if (q.limit() <= 0) {
            q.limit(100);
        }

        // 查询列表
        List<WnObj> list = io.query(q);

        // 需要分页
        if (isPaging) {
            WnPagerObj pager = new WnPagerObj().setBy(q);
            long tc = io.count(q);
            pager.setTotal(tc);
            return new NutMap("list", list).setv("pager", pager);
        }

        // 直接返回列表
        return list;
    }

    @At("/children")
    public Object children(@Param("str") String str, @Param("pg") Boolean paging) {
        WnAuthSession se = Wn.WC().checkSession();
        WnObj o = Wn.checkObj(io, se, str);

        // 查询
        List<WnObj> list = io.getChildren(o, null);

        // 不要翻页信息
        if (!paging)
            return list;

        // 更新分页信息
        int limit = Math.max(100, list.size());
        WnPagerObj pager = new WnPagerObj().set(limit, 0);
        pager.setTotal(list.size());

        // 返回
        return new NutMap("list", list).setv("pager", pager);
    }

    /**
     * 给定查询对象，获取对象列表
     * 
     * @param limit(`_l`)
     *            查询数据上限
     * @param skip(`_o`)
     *            跳过的记录数
     * @param sort(`_s`)
     *            一个排序的 JSON 字符串，形式类似 <code>"nm:-1,ct:1"</code>
     * @param mine(`_me`)
     *            标识仅仅查询自己主域的内容，默认 true
     * @param match(`..`)
     *            其余的全部参数（除了 `_`开头的）均被作为查询参数
     * @return
     * 
     *         <pre>
     * {
     *  list:[..], 
     *  pager: {
     *      pageNumber,
     *      pageSize,
     *      pageCount,
     *      totalCount
     *  }
     * }
     *         </pre>
     */
    @At("/find")
    public Object find(@Param("_l") int limit,
                       @Param("_o") int skip,
                       @Param("_s") String sort,
                       @Param(value = "_me", df = "true") boolean mine,
                       @Param("..") NutMap match) {
        // 准备查询
        WnQuery q = new WnQuery();

        // 翻页信息
        WnPagerObj pager = new WnPagerObj().set(limit, skip);
        pager.setupQuery(q);

        // 排序
        NutMap sorting = Lang.map(sort);
        q.sort(sorting);

        // 确保去掉 "_" 开头的字段
        List<String> dks = new ArrayList<>(match.size());
        for (String k : match.keySet()) {
            if (k.startsWith("_"))
                dks.add(k);
        }
        for (String k : dks) {
            match.remove(k);
        }

        // 添加查询条件
        q.setAll(match);

        // 增加自己主域限制
        if (mine) {
            WnAuthSession se = Wn.WC().checkSession();
            q.setv("d0", "home");
            q.setv("d1", se.getMyGroup());
        }

        // 查询
        List<WnObj> list = io.query(q);

        // 更新分页信息
        long tc = io.count(q);
        pager.setTotal(tc);

        // 返回
        return new NutMap("list", list).setv("pager", pager);

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
                              @Param("aph") boolean isAbsolutePath,
                              @Param("sh") int sizeHint,
                              @Param("scale") String scale,
                              @Param("f") boolean force,
                              @Param("bg") String bg) {
        // 缩略图对象
        BufferedImage im = null;

        // 统一设定默认缩略图的格式
        String mime = "image/png";
        String imtp = "png";

        // 确保 str 的形式正确
        str = __format_str(str, isAbsolutePath);

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
            Matcher m = Regex.getPattern("^(\\d+)[xX](\\d+)$").matcher(scale);
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
        String ph = Wn.normalizeFullPath("~/.thumbnail/dft/" + tp + "/" + sz_key + ".png",
                                         Wn.WC().checkSession());
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
     * 得到一个对象的内容
     * 
     * @param str
     *            对象字符串。@see {@link Wn#checkObj}
     * @param download
     *            下载模式。 @see {@link WnWeb#autoUserAgent(WnObj, String, String)}
     * @return 对象的内容
     * 
     */
    @At("/content")
    @AdaptBy(type = JsonAdaptor.class)
    public View content(@Param("str") String str,
                        @Param("d") String download,
                        @ReqHeader("User-Agent") String ua,
                        @ReqHeader("If-None-Match") String etag,
                        @ReqHeader("Range") String range,
                        HttpServletRequest req,
                        HttpServletResponse resp) {
        // 获取当前会话
        WnAuthSession se = Wn.WC().checkSession();

        // 取得对应对象
        WnObj o = Wn.checkObj(io, se, str);

        // 确保可读，同时处理链接文件
        o = Wn.WC().whenRead(o, false);

        // 校验 sha1
        // TODO 如果先生成一个对象，然后浏览器读取了，在服务器上将这个对象改成一个链接对象
        // 再次 read 时，因为浏览器端 Wn.read 会带上 sha1 校验，就 400 了。
        // 先暂时关掉校验功能，之后再想想怎么弄比较好
        // if (!Strings.isBlank(sha1)) {
        // if (!o.isSameSha1(sha1))
        // return new HttpStatusView(400);
        // }

        // 纠正一下下载模式
        ua = WnWeb.autoUserAgent(o, ua, download);

        // 返回下载视图
        return new WnObjDownloadView(io, o, ua, etag, range);

    }

    @At("/save/text")
    public WnObj saveText(@Param("str") String str, @Param("content") String content) {
        // 获取当前会话
        WnAuthSession se = Wn.WC().checkSession();

        // 取得对应对象
        WnObj o = Wn.checkObj(io, se, str);

        // 确保可读，同时处理链接文件
        o = Wn.WC().whenRead(o, false);

        // 处理空
        content = Strings.sNull(content, "");

        // 写入
        io.writeText(o, content);

        // 返回
        return o;
    }

    /**
     * @param str
     *            上传目标，可以是目录也可以是文件，根据 <code>mode</code>来决定
     * @param mode
     *            保存模式
     *            <ul>
     *            <li><code>r</code>- 替换模式：<code>str</code>可以存在，如果是目录根据
     *            <code>nm</code>创建。如果不存在，如果以 <code>/</code> 结尾则被当做目录，否则是目标文件路径
     *            <li><code>s</code>- 严格模式: <code>str</code>必须存在，且是一个文件，将会将其替换
     *            <li><code>a</code>- 追加模式:
     *            <code>str</code>必须存在，且必须是目录，如果是文件选择其目录。<br>
     *            并根据文件名模板（<code>dupp</code>）生成一个确定不存在的文件名
     *            </ul>
     * @param nm
     *            本地文件名
     * @param sz
     *            本地文件尺寸
     * @param mime
     *            本地文件的内容类型
     * @param tmpl
     *            只有在 <code>mode=a</code> 模式下才有效:
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
     *            </pre>
     * 
     *            默认的，本参数值为 <code>"${major}(${nb})${suffix}"</code>
     * @param ins
     *            上传的数据流
     * @return 写入的文件对象
     */
    @At("/save/stream")
    @AdaptBy(type = QueryStringAdaptor.class)
    public WnObj saveStream(@Param("str") String str,
                            @Param("nm") String nm,
                            @Param("m") String mode,
                            @Param("sz") long sz,
                            @Param("mime") String mime,
                            @Param("tmpl") String tmpl,
                            InputStream ins) {
        // 得到当前会话
        WnAuthSession se = Wn.WC().checkSession();

        // 取得对应对象
        WnObj o = Wn.getObj(io, se, str);

        // 处理链接文件
        if (null != o)
            o = Wn.real(o, io, new HashMap<>());

        // 默认模式
        mode = Strings.sBlank(mode, "r");

        // r- 替换模式：str可以存在，如果不存在或是目录根据 nm创建
        if ("r".equals(mode)) {
            // 不存在，那么创建
            if (null == o) {
                // 目标是个目录
                if (str.endsWith("/")) {
                    String aph = Wn.normalizeFullPath(Wn.appendPath(str, nm), se);
                    o = io.createIfNoExists(null, aph, WnRace.FILE);
                }
                // 目标是个文件
                else {
                    String aph = Wn.normalizeFullPath(str, se);
                    o = io.createIfNoExists(null, aph, WnRace.FILE);
                }
            }
            // 存在，且是目录，那么还是创建
            else if (o.isDIR()) {
                o = io.createIfNoExists(o, nm, WnRace.FILE);
            }
        }
        // s- 严格模式: str必须存在，且是一个文件，将会将其替换
        else if ("s".equals(mode)) {
            if (null == o) {
                throw Er.create("e.web.obj.save_stream.targetNoExists", str);
            }
            if (!o.isFILE()) {
                throw Er.create("e.web.obj.save_stream.targetNoFile", str);
            }
        }
        // a- 追加模式: str必须存在，且必须是目录，如果是文件选择其目录。
        // 并根据文件名模板（dupp）生成一个确定不存在的文件名
        else if ("a".equals(mode)) {
            if (null == o) {
                throw Er.create("e.web.obj.save_stream.targetNoExists", str);
            }
            WnObj oP;
            String fname;
            // 目录的话，则按照 nm取一下
            if (o.isDIR()) {
                oP = o;
                o = io.fetch(o, nm);
                fname = nm;
            }
            // 文件的话，取一下父目录
            else {
                oP = o.parent();
                fname = o.name();
            }
            // 下面这个逻辑是去重的，因为的追加模式，所以需要一直确保找到一个文件名在给定目录下不存在
            int i = 1;
            while (null != o) {
                // 准备查找一个不存在的文件名
                NutMap c = new NutMap();
                c.put("major", Files.getMajorName(nm));
                c.put("suffix", Files.getSuffix(nm));
                Tmpl seg = Tmpl.parse(tmpl);
                c.put("nb", i++);
                fname = seg.render(c);
                // 一直找到一个不存在的名称
                if (!io.exists(oP, fname))
                    break;
            }
            // 创建这个文件
            o = io.create(oP, fname, WnRace.FILE);
        }
        // 错误的模式
        else {
            throw Er.create("e.web.obj.save_stream.invalidMode", mode);
        }

        // 写入
        OutputStream ops = io.getOutputStream(o, 0);
        Streams.writeAndClose(ops, ins, 256 * 1024);

        // 计入原始数据
        NutMap localMeta = new NutMap();
        localMeta.put("name", nm);
        localMeta.put("mime", mime);
        localMeta.put("size", sz);
        io.appendMeta(o, Lang.map("local", localMeta));

        // 返回
        return o;
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
     * @param downloadMedia
     *            媒体文件是否要下载，默认不下载
     * @param forceDownload
     *            是否是强制下载模式，如果是下载，将提供 CONTENT_DISPOSITION 响应头 默认 false
     * 
     * @param isAbsolutePath
     *            给的对象字符串是否是绝对路径，如果是，则必须确保 "/" 或者 "~" 开头
     * 
     * @param ua
     *            客户端类型，下载模式下才生效，主要是根据浏览器的不同，生成下载目标的响应头（Safari的乱码问题）
     * 
     * @return 对象当前的内容输入流
     * 
     * @see org.nutz.walnut.util.Wn#checkObj(org.nutz.walnut.api.io.WnIo,
     *      String)
     */
    @Deprecated
    @At("/read/**")
    @Ok("void")
    public View read(String str,
                     @Param("sha1") String sha1,
                     @Param("d") boolean downloadMedia,
                     @Param("fd") boolean forceDownload,
                     @Param("aph") boolean isAbsolutePath,
                     @ReqHeader("User-Agent") String ua,
                     @ReqHeader("If-None-Match") String etag,
                     @ReqHeader("Range") String range,
                     HttpServletRequest req,
                     HttpServletResponse resp) {
        // 截取参数
        int pos = str.lastIndexOf('?');
        if (pos > 0) {
            str = str.substring(0, pos);
        }

        // 确保 str 的形式正确
        str = __format_str(str, isAbsolutePath);
        // 防御非法请求
        if (Strings.isBlank(str) || str.equals("id:") || str.equals("id:undefined")) {
            return HttpStatusView.HTTP_404;
        }

        // 首先得到目标对象
        WnObj o = Wn.checkObj(io, Wn.WC().checkSession(), str);

        // 确保可读，同时处理链接文件
        o = Wn.WC().whenRead(o, false);

        // 校验 sha1
        // TODO 如果先生成一个对象，然后浏览器读取了，在服务器上将这个对象改成一个链接对象
        // 再次 read 时，因为浏览器端 Wn.read 会带上 sha1 校验，就 400 了。
        // 先暂时关掉校验功能，之后再想想怎么弄比较好
        // if (!Strings.isBlank(sha1)) {
        // if (!o.isSameSha1(sha1))
        // return new HttpStatusView(400);
        // }

        // 非强制下载，检查一下
        if (!forceDownload) {
            // 特殊的类型，将不生成下载目标
            ua = WnWeb.autoUserAgent(o, ua, downloadMedia);
        }

        // 返回下载视图
        return new WnObjDownloadView(io, o, ua, etag, range);
    }

    @Deprecated
    @POST
    @AdaptBy(type = QueryStringAdaptor.class)
    @At("/write/**")
    public WnObj write(String str, @Param("aph") boolean isAbsolutePath, InputStream ins) {
        // 确保 str 的形式正确
        str = __format_str(str, isAbsolutePath);

        // 取得对象
        WnObj o = Wn.checkObj(io, str);

        // 确保可写，同时处理链接文件
        o = Wn.WC().whenWrite(o, false);

        // 写入
        OutputStream ops = io.getOutputStream(o, 0);
        Streams.writeAndClose(ops, ins, 256 * 1024);

        // 确保有路径
        o.path();
        return o;
    }

    /**
     * 从本地上传一个文件流
     * 
     * @param str
     *            对象字符串。@see
     *            {@link #checkObj(String, org.nutz.lang.util.NutMap, String)}
     * @param isAbsolutePath
     *            说明参数 str 是否是绝对路径。如果是绝对路径，将会确保 str 参数是以 <code>/</code> 开头的
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
    @Deprecated
    @At("/upload/**")
    @AdaptBy(type = QueryStringAdaptor.class)
    public WnObj upload(String str,
                        @Param("aph") boolean isAbsolutePath,
                        @Param("nm") String nm,
                        @Param("cie") boolean isCreateIfNoExists,
                        @Param("race") WnRace race,
                        @Param("sz") long sz,
                        @Param("mime") String mime,
                        @Param("dupp") String dupp,
                        InputStream ins) {
        // 得到当前会话
        WnAuthSession se = Wn.WC().checkSession();

        // 首先得到目标对象
        WnObj ta;
        if (isCreateIfNoExists && str.contains("/")) {
            // 确保 str 的形式正确
            str = __format_str(str, isAbsolutePath);
            String ph = Wn.normalizeFullPath(str, se);
            ta = io.createIfNoExists(null, ph, race);
        }
        // 通常为 id:xxx 形式的对象
        else {
            ta = Wn.checkObj(io, se, str);
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
                    NutMap c = new NutMap();
                    c.put("major", Files.getMajorName(nm));
                    c.put("suffix", Files.getSuffix(nm));
                    Tmpl seg = Tmpl.parse(dupp);
                    int i = 1;
                    do {
                        c.put("nb", i++);
                        fname = seg.render(c);
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
        Streams.writeAndClose(ops, ins, 256 * 1024);

        // 返回
        return o;
    }

    private String __format_str(String str, boolean isAbsolutePath) {
        // 绝对目录 会丢失第一个字符 /
        if (isAbsolutePath
            && !str.startsWith("/")
            && !str.startsWith("~")
            && !str.startsWith("id:")) {
            str = "/" + str;
        }
        return str;
    }
}
