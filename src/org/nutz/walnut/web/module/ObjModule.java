package org.nutz.walnut.web.module;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
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
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.web.filter.WnCheckSession;
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

    /**
     * 得到对象内容
     * 
     * @param str
     *            对象字符串。@see
     *            {@link #checkObj(String, org.nutz.lang.util.NutMap, String)}
     * 
     * @return 对象当前的内容输入流
     */
    @At("/read/**")
    @Ok("void")
    public View read(String str) {
        // 首先得到目标对象
        WnObj o = Wn.checkObj(io, str);
        return new WnObjDownloadView(io, o);
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
