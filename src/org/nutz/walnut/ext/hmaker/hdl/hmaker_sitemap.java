package org.nutz.walnut.ext.hmaker.hdl;

import java.io.Writer;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.lang.ContinueLoop;
import org.nutz.lang.Each;
import org.nutz.lang.ExitLoop;
import org.nutz.lang.Lang;
import org.nutz.lang.LoopException;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.segment.CharSegment;
import org.nutz.lang.segment.Segment;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Tag;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.WnThingService;
import org.nutz.walnut.ext.thing.util.ThQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(regex = "^(json|xml|txt|write)$")
public class hmaker_sitemap implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 首先,检查有没有发布
        WnObj wwwHome = sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys));
        if (wwwHome.has("hm_target_release")) {
            wwwHome = sys.io.check(null,
                                   Wn.normalizeFullPath(wwwHome.getString("hm_target_release"),
                                                        sys));
        }
        // 检查域名是否需要传入
        String host = hc.params.get("host");
        if (Strings.isBlank(host) && wwwHome.has("www")) {
            host = wwwHome.getArray("www", String.class)[0];
        } else {
            sys.err.print("e.cmd.hmaker.sitemap.need_host");
            return;
        }
        if (!host.startsWith("http"))
            host = "http://" + host;
        String _host = host;
        String wwwPath = wwwHome.path();
        List<NutMap> links = new LinkedList<>();
        // 遍历发布目录,查找全部网页
        sys.io.walk(wwwHome, new Callback<WnObj>() {
            public void invoke(WnObj obj) {
                if (obj.isDIR())
                    return;
                if (!obj.isMime("text/html")) // 非网页就无视吧,虽然google扩展协议支持图片和视频
                    return;
                String ph = obj.path().substring(wwwPath.length());
                // 截断后应该是/开头的,这里只是防御一下
                if (!ph.startsWith("/"))
                    ph = "/" + ph;
                // wnml后缀?改成更友好的html后缀咯
                if (ph.endsWith(".wnml"))
                    ph = ph.substring(0, ph.length() - 4) + "html";
                // index.html就是欢迎页, 给目录完事
                if (ph.endsWith("/index.html"))
                    ph = ph.substring(0, ph.length() - "index.html".length());
                // 如果包含{{XXX}},展开之
                if (obj.is("hm_pg_args", true) && obj.get("hm_pg_args_tsid") != null) {
                    Segment seg = new CharSegment(ph.replace("{{", "${").replace("}}", "}"));
                    // hm_pg_args_tsid可能是个数组
                    Lang.each(obj.get("hm_pg_args_tsid"), new Each<Object>() {
                        public void invoke(int index, Object ele, int length)
                                throws ExitLoop, ContinueLoop, LoopException {
                            String tid = String.valueOf(ele);
                            WnThingService wts = new WnThingService(sys.io, sys.io.checkById(tid));
                            // 遍历thing的数据,展开路径
                            // TODO 容易爆
                            for (WnObj oT : wts.queryList(new ThQuery())) {
                                String ph = seg.render(Lang.context(oT)).toString();
                                links.add(new NutMap("loc", _host + ph).setv("lastmod",
                                                                             oT.lastModified()));
                            }
                        }
                    });
                } else {
                    links.add(new NutMap("loc", _host + ph).setv("lastmod", obj.lastModified()));
                }
            }
        }, WalkMode.DEPTH_NODE_FIRST);

        // 看看是不是需要直接写文件
        boolean doWrite = hc.params.is("write");
        // JSON格式, 对应sitemap.json, 搜索引擎并不读取.
        if (hc.params.is("json")) {
            if (doWrite) {
                sys.io.writeJson(sys.io.createIfNoExists(wwwHome, "sitemap.json", WnRace.FILE),
                                 links,
                                 JsonFormat.full());
            } else {
                sys.out.writeJson(links, JsonFormat.full());
            }
        }
        // XML格式, 当前的流行格式了,对应 sitemap.xml
        else if (hc.params.is("xml")) {
            Tag urlset = Tag.tag("urlset");
            StringBuilder sb = new StringBuilder();
            for (NutMap link : links) {
                Tag url = urlset.add("url");
                url.add("loc").setText(link.getString("loc"));
                url.add("lastmod").setText(Times.sD(new Date(link.getLong("lastmod"))));
            }
            urlset.toXml(sb, 0);
            if (doWrite)
                sys.io.writeText(sys.io.createIfNoExists(wwwHome, "sitemap.xml", WnRace.FILE),
                                 sb.toString());
            else
                sys.out.print(sb.toString());
        }
        // 默认 Txt格式,一行一个网址,最古老的模式,文件名sitemap.txt
        else {
            if (doWrite) {
                WnObj txt = sys.io.createIfNoExists(wwwHome, "sitemap.txt", WnRace.FILE);
                if (txt.len() > 0)
                    sys.io.writeText(txt, "");
                try (Writer w = sys.io.getWriter(txt, 0)) {
                    for (NutMap link : links) {
                        w.append(link.getString("loc")).append("\r\n");
                    }
                    w.flush();
                }
            } else {
                for (NutMap link : links) {
                    sys.out.println(link.get("loc"));
                }
            }
        }
    }

}