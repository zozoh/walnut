package com.site0.walnut.web2.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.nutz.lang.Files;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;

import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.login.session.WnSession;
import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcView;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Wobj;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.tmpl.WnTmpl;

/**
 * <code>/o/save/stream</code>：对照旧 <code>ObjModule.saveStream(704)</code>（RM-NUTZ-08
 * 审核表 #10）。旧 <code>@AdaptBy(QueryStringAdaptor)</code>：参数只走 query string，
 * 请求体原始流即上传数据。逐行照抄旧方法体（<code>Wobj.normalizeName</code>/
 * <code>Wn.real</code>/r-s-a 分支/<code>WnTmpl</code> 去重，一行不省），只换绑定/视图/
 * 响应写法。过滤器 <code>SessionCheckFilter()</code>。
 */
public class ObjSaveStreamController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        WnMvcRequest req = ctx.request();
        // 这个接口开放给外部 app 调用
        cors(ctx);

        // Options 无视
        if (options(ctx)) {
            return null;
        }
        try {
            NutMap qs = req.queryStringMap();
            String str = qs.getString("str");
            String nm = qs.getString("nm");
            String mode = qs.getString("m");
            long sz = qs.getLong("sz", 0);
            String mime = qs.getString("mime");
            String prefix = qs.getString("prefix");
            String tmpl = qs.getString("tmpl");
            InputStream ins = req.getInputStream();

            // 得到当前会话
            WnSession se = Wn.WC().checkSession();
            WnIo io = io(ctx);

            // 获取绝对路径
            String aph = Wn.normalizeFullPath(str, se);

            // 取得对应对象
            WnObj o = io.fetch(null, aph);

            // 格式化一下，去掉文件名里奇怪的字符
            nm = Wobj.normalizeName(nm);
            String localName = nm;
            boolean hasPrefix = !Ws.isBlank(prefix);
            if (hasPrefix) {
                nm = prefix + nm;
            }

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
                        aph = Wn.appendPath(aph, nm);
                        o = io.createIfNoExists(null, aph, WnRace.FILE);
                    }
                    // 目标是个文件
                    else {
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
                WnTmpl seg = WnTmpl.parse(tmpl);
                while (null != o) {
                    // 准备查找一个不存在的文件名
                    NutMap c = new NutMap();
                    c.put("major", Files.getMajorName(nm));
                    c.put("suffix", Files.getSuffix(nm));
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
            localMeta.put("name", localName);
            localMeta.put("mime", mime);
            localMeta.put("size", sz);
            NutMap meta = Wlang.map("local", localMeta);

            // 设置标题
            String title = Files.getMajorName(localName);
            meta.put("title", title);

            // 写入数据
            io.appendMeta(o, meta);

            // 返回
            return WnMvcView.ajax(o);
        }
        catch (RuntimeException e) {
            return businessFailure(e);
        }
    }

}
