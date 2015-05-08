package org.nutz.walnut.web.module;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
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
import org.nutz.mvc.annotation.Param;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnSession;
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
        io.appendMeta(o, map);
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
        WnSession se = Wn.WC().checkSE();
        WnObj o = Wn.checkObj(io, se, str);
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
     * @param ins
     *            文件内容流
     * @return 创建的对象
     */
    @At("/upload/**")
    @AdaptBy(type = QueryStringAdaptor.class)
    public WnObj upload(String str,
                        @Param("nm") String nm,
                        @Param("sz") long sz,
                        @Param("mime") String mime,
                        InputStream ins) {
        // 首先得到目标对象
        WnObj ta = Wn.checkObj(io, str);

        WnObj o;
        // 如果目标对象是个文件
        if (ta.isFILE()) {
            o = ta;
        }
        // 如果是个目录，则试图创建一个新文件
        else if (ta.isDIR()) {
            String fname = nm;
            String majorName = Files.getMajorName(nm);
            String suffix = Files.getSuffixName(nm);
            if (!Strings.isBlank(suffix))
                suffix = "." + suffix;
            else
                suffix = "";
            // 如果遇到重名的文件，则生成一个新的名字
            int nb = 1;
            while (io.fetch(ta, fname) != null) {
                fname = String.format("%s(%d)%s", majorName, nb++, suffix);
            }

            // 创建文件对象
            o = io.create(ta, fname, WnRace.FILE);
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
