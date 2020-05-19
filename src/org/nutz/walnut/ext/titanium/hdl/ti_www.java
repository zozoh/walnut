package org.nutz.walnut.ext.titanium.hdl;

import java.util.List;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.www.JvmWnmlRuntime;
import org.nutz.walnut.ext.www.WnmlRuntime;
import org.nutz.walnut.ext.www.WnmlService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value = "Q", regex = "^wnml$")
public class ti_www implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // .........................................
        // 准备计时
        Stopwatch sw = Stopwatch.begin();
        // 分析参数
        String phSrc = hc.params.val_check(0);
        String phDist = hc.params.val_check(1);
        boolean transWnml = hc.params.is("wnml");
        String wnmlSetting = null;
        if (transWnml) {
            // 有木有直接声明呢
            wnmlSetting = hc.params.getString("vars");
            // 看看是不是来自一个文件或者标准输入
            if ("true".equals(wnmlSetting) || Strings.isBlank(wnmlSetting)) {
                String ivar = hc.params.getString("ivar");
                if (!Strings.isBlank(ivar)) {
                    WnObj oVars = Wn.checkObj(sys, ivar);
                    wnmlSetting = sys.io.readText(oVars);
                }
                // 试试标准输入吧
                else {
                    wnmlSetting = sys.in.readAll();
                }
            }
        }

        boolean quiet = hc.params.is("Q");

        // .........................................
        // 得到关键目录
        WnObj oSrc = Wn.checkObj(sys, phSrc);
        WnObj oWWW = Wn.checkObj(sys, phDist);
        if (!quiet) {
            sys.out.printlnf("Ti www:\n - src : %s\n - dist: %s",
                             oSrc.getFormedPath(true),
                             oWWW.getFormedPath(true));
            sys.out.println("copy resources:");
        }
        sw.tag("EvalPath");

        // .........................................
        // Copy 站点资源
        int mode = Wn.Io.RECUR;
        if (!quiet) {
            mode |= Wn.Io.VERBOSE;
        }

        // .........................................
        List<WnObj> tops = sys.io.getChildren(oSrc, null);
        WnObj oWnml = null, oSiteState = null;
        for (WnObj oTop : tops) {
            // 要转换？ 嗯， site-state.json 稍后再处理吧
            if (transWnml && oTop.isSameName("site-state.json")) {
                oSiteState = oTop;
            }
            // 要转换？ 嗯， index.wnml 稍后再处理吧
            else if (transWnml && oTop.isSameName("index.wnml")) {
                oWnml = oTop;
            }
            // 傻傻的 Copy 吧
            else {
                String ph = oWWW.getRegularPath() + oTop.name();
                Wn.Io.copy(sys, mode, oTop, ph);
            }
        }
        sw.tag("CopyResources");

        if (transWnml) {
            // .......................................
            // 准备标准上下文
            // {
            // # 网站的页面资源基础路径
            // pageBase : "/",
            // # HTTP接口的基础路径
            // apiBase : "/api/",
            // # CDN 服务器的基础路径
            // cdnBase : null,
            // # 静态资源基础路径
            // rs : "/gu/rs/",
            //
            // # 预加载资源列表，没有的话，请保持空数组 []
            // preloads : ["@dist:ti-more-all.js"]
            // }
            NutMap vars = Lang.map(wnmlSetting);
            vars.put("CURRENT_DIR", oWWW.getFormedPath(true));
            vars.put("PAGE_BASE", vars.get("pageBase"));

            // .......................................
            // Copy site-state.json
            if (!quiet) {
                sys.out.println("Gen site-state.json:");
            }
            doSiteState(sys, quiet, vars, oSrc, oWWW, oSiteState);
            sw.tag("SiteState");

            // .......................................
            // 转换 wnml
            WnObj oIndex = doIndexWnml(sys, quiet, vars, oSrc, oWWW, oWnml);
            sw.tag("IndexWnml");

            // .........................................
            // 如果开启了 -wnml， 实体化的虚页路径
            if (!quiet) {
                sys.out.println("Gen virtual pages:");
            }
            String[] vPages = Strings.splitIgnoreBlank(hc.params.getString("vpages", "page"));
            for (String vPage : vPages) {
                if (!quiet) {
                    sys.out.printf("@%s:\n", vPage);
                }
                WnObj oPageDir = sys.io.check(oWWW, vPage);
                sys.io.walk(oPageDir, (o) -> {
                    String name = Files.getMajorName(o.name()) + ".html";
                    if (!quiet) {
                        String rph = Wn.Io.getRelativePath(oPageDir, o);
                        sys.out.printf("  -> %s => %s\n", rph, name);
                    }
                    WnObj oPage = sys.io.createIfNoExists(o.parent(), name, WnRace.FILE);
                    Wn.Io.copyFile(sys.io, oIndex, oPage);
                }, WalkMode.LEAF_ONLY);
            }
        }

        // 结束
        sw.stop();
        if (!quiet) {
            sys.out.println(sw.toString());
        }
    }

    private WnObj doIndexWnml(WnSystem sys,
                              boolean quiet,
                              NutMap vars,
                              WnObj oSrc,
                              WnObj oWWW,
                              WnObj oWnml) {
        if (!quiet) {
            sys.out.println("Gen index.html");
        }
        // 读取文件
        String input = sys.io.readText(oWnml);

        // 准备转换上下文
        NutMap context = new NutMap();
        String rootPath = oWWW.path();

        context.put("WWW", oWWW.pickBy("^(id|hm_.+)$"));
        context.put("SITE_HOME", rootPath);
        context.put("grp", sys.getMyGroup());
        context.putAll(vars);

        // 准备转换服务类
        WnmlRuntime wrt = new JvmWnmlRuntime(sys);
        WnmlService ws = new WnmlService();

        // 执行转换
        WnObj oIndex = sys.io.createIfNoExists(oWWW, "index.html", WnRace.FILE);
        String html = ws.invoke(wrt, context, input);
        sys.io.writeText(oIndex, html);

        if (!quiet) {
            sys.out.println(" - done");
        }

        return oIndex;

    }

    private void doSiteState(WnSystem sys,
                             boolean quiet,
                             NutMap vars,
                             WnObj oSrc,
                             WnObj oDist,
                             WnObj oSiteState) {
        WnObj oTaSS = sys.io.createIfNoExists(oDist, "site-state.json", WnRace.FILE);

        NutMap map = sys.io.readJson(oSiteState, NutMap.class);
        map.put("base", vars.get("pageBase"));
        map.put("apiBase", vars.get("apiBase"));
        map.put("cdnBase", vars.get("cdnBase"));

        sys.io.writeJson(oTaSS, map, JsonFormat.full());
        if (!quiet) {
            JsonFormat jfmt = JsonFormat.compact().setQuoteName(false);
            String brief = Json.toJson(vars.pick("pageBase", "apiBase", "cdnBase"), jfmt);
            sys.out.printlnf(" + site-state.json : {%s}", brief);
        }

    }

}
