package org.nutz.walnut.ext.hmaker.hdl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.Disks;
import org.nutz.log.Log;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("ocqnlbish")
public class hmaker_lib implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到站点主目录
        // WnObj oSiteHome = Hms.checkSiteHome(sys, hc.params.val_check(0));
        WnObj oSiteHome = hc.oRefer;
        // ................................................
        // 如果是写入的话，会自动创建库目录
        if (hc.params.has("write")) {
            __do_write(sys, hc, oSiteHome);
            return;
        }
        // ................................................
        // 得到库的主目录
        WnObj oLibHome = sys.io.fetch(oSiteHome, "lib");
        // ................................................
        // 读取某个库文件
        if (hc.params.has("read")) {
            __do_read(sys, hc, oLibHome);
            return;
        }

        // ................................................
        // 删除某个库文件
        if (hc.params.has("del")) {
            __do_del(sys, hc, oLibHome);
            return;
        }
        // ................................................
        // 输出某个库文件元数据
        if (hc.params.has("get")) {
            __do_get(sys, hc, oLibHome);
            return;
        }
        // ................................................
        // 列出关联的页面
        if (hc.params.has("pages")) {
            __do_pages(sys, hc, oSiteHome);
            return;
        }
        // ................................................
        // 库改名
        if (hc.params.has("rename")) {
            __do_rename(sys, hc, oSiteHome, oLibHome);
            return;
        }
        // ................................................
        // 默认列库名
        __do_list(sys, hc, oLibHome);
    }

    private void __do_rename(WnSystem sys, JvmHdlContext hc, WnObj oSiteHome, WnObj oLibHome) {
        // 准备日志输出接口
        Log log = sys.getLog(hc.params);
        Stopwatch sw = Stopwatch.begin();

        log.infof("%%[0/5] rename lib for site : %s", oSiteHome.name());

        // 进行改名
        if (null != oLibHome) {
            // 得到库对象
            String libName = hc.params.get("rename");
            WnObj oLib = sys.io.check(oLibHome, libName);

            // 得到新名称
            String newnm = hc.params.val_check(1);

            // 两个名称如果不相等，则改名
            if (!libName.equals(newnm)) {
                // 首先改动库对象的新名称
                sys.io.rename(oLib, newnm, true);
                log.infof("%%[1/5] rename '%s' -> '%s'", libName, newnm);

                // 查到关联页面的列表
                List<WnObj> oPageList = this.__query_refer_pages(sys, oSiteHome, libName);

                // 开始循环处理
                int sum = oPageList.size() + 1;
                int n = 0;
                for (WnObj oPage : oPageList) {
                    // 计数
                    n++;

                    // 打印日志
                    String rph = Disks.getRelativePath(oSiteHome.path(), oPage.path());
                    log.infof("%%[%d/%d] - %s", n, sum, rph);

                    // 解析页面
                    String html = sys.io.readText(oPage);
                    Document doc = Jsoup.parse(html);

                    // 处理对应的 lib
                    Elements eleLibs = doc.body().select(".hm-com[lib=\"" + libName + "\"]");
                    for (Element ele : eleLibs) {
                        ele.attr("lib", newnm);
                    }

                    // 写入页面
                    html = doc.body().html();
                    sys.io.writeText(oPage, html);

                    // 同步元数据
                    Hms.syncPageMeta(sys, oPage, html);
                }

                // 最后更新
                log.infof("%%[%d/%d] - %d items be updated", sum, sum, oPageList.size());
            }
        }
        // 结束
        sw.stop();
        log.infof("%%[-1/0] All done in %dms", sw.getDuration());
    }

    private void __do_pages(WnSystem sys, JvmHdlContext hc, WnObj oSiteHome) {
        if (null != oSiteHome) {
            List<WnObj> list = __query_refer_pages(sys, oSiteHome, hc.params.get("pages"));
            // 输出
            Cmds.output_objs(sys, hc.params, null, list, false);
        }
    }

    private List<WnObj> __query_refer_pages(WnSystem sys, WnObj oSiteHome, String libName) {
        WnQuery q = new WnQuery();
        q.setv("d0", oSiteHome.d0());
        q.setv("d1", oSiteHome.d1());
        q.setv("hm_site_id", oSiteHome.id());
        q.setv("hm_libs", libName);
        List<WnObj> list = sys.io.query(q);
        return list;
    }

    private void __do_get(WnSystem sys, JvmHdlContext hc, WnObj oLibHome) {
        if (null != oLibHome) {
            WnObj oLib = sys.io.check(oLibHome, hc.params.get("get"));
            // 输出
            sys.out.println(Json.toJson(oLib, hc.jfmt));
        }
        // 否则直接输出错误
        else {
            throw Er.create("e.cmd.hmaker.lib.noexists", "/lib");
        }
    }

    private void __do_del(WnSystem sys, JvmHdlContext hc, WnObj oLibHome) {
        if (null != oLibHome) {
            WnObj oLib = sys.io.check(oLibHome, hc.params.get("del"));
            sys.io.delete(oLib);
            // 输出
            if (hc.params.is("o")) {
                sys.out.println(Json.toJson(oLib, hc.jfmt));
            }
        }
    }

    private void __do_read(WnSystem sys, JvmHdlContext hc, WnObj oLibHome) {
        if (null != oLibHome) {
            WnObj oLib = sys.io.check(oLibHome, hc.params.get("read"));
            String content = sys.io.readText(oLib);
            sys.out.print(content);
        } else {
            throw Er.create("e.cmd.hmaker.lib.noLibHome");
        }
    }

    private void __do_list(WnSystem sys, JvmHdlContext hc, WnObj oLibHome) {
        // 先读取所有的库文件
        List<WnObj> oLibs = new LinkedList<>();
        if (null != oLibHome) {
            sys.io.walk(oLibHome, new Callback<WnObj>() {
                public void invoke(WnObj oLib) {
                    oLibs.add(oLib);
                }
            }, WalkMode.LEAF_ONLY);
        }

        // 准备输出
        String listMode = hc.params.get("list");

        // JSON 对象列表
        if ("obj".equals(listMode)) {
            sys.out.println(Json.toJson(oLibs, hc.jfmt));
        }
        // 名称列表
        else {
            // 首先计算名称
            List<String> nms = new ArrayList<>(oLibs.size());
            if (oLibs.size() > 0) {
                String libHomePath = oLibHome.path();
                for (WnObj oLib : oLibs) {
                    String rph = Disks.getRelativePath(libHomePath, oLib.path());
                    nms.add(rph);
                }
            }
            // 仅仅是对象名，一行一个
            if ("name".equals(listMode)) {
                sys.out.println(Lang.concat("\n", nms));
            }
            // 默认是一个 JSON 数据组，元素是库文件名
            else {
                sys.out.println(Json.toJson(nms));
            }
        }
    }

    private void __do_write(WnSystem sys, JvmHdlContext hc, WnObj oSiteHome) {
        WnObj oLib;
        String libName = hc.params.get("write");
        oLib = sys.io.createIfNoExists(oSiteHome, "lib/" + libName, WnRace.FILE);

        // 确保类型为指定类型
        if (!oLib.isType("hm_lib")) {
            oLib.type("hm_lib");
            sys.io.set(oLib, "^tp$");
        }

        // 得到内容
        String content = hc.params.get("content");
        // 从标准输入得到内容
        if ("true".equals(content) || Strings.isBlank(content)) {
            content = null;
        }
        // 从文件得到内容
        if (null == content && hc.params.has("file")) {
            WnObj o = Wn.checkObj(sys, hc.params.get("file"));
            content = sys.io.readText(o);
        }
        // 默认从标准输入读取
        if (null == content) {
            content = sys.in.readAll();
        }
        // 写入内容
        sys.io.writeText(oLib, content);

        // .................................................
        // 分析页面内容看看都使用了哪些组件
        Hms.syncPageMeta(sys, oLib, content);

        // 是否输出?
        if (hc.params.is("o")) {
            sys.out.println(Json.toJson(oLib, hc.jfmt));
        }
    }

}
