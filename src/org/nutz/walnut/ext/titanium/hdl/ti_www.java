package org.nutz.walnut.ext.titanium.hdl;

import java.util.List;

import org.nutz.json.JsonFormat;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
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

@JvmHdlParamArgs(value = "Q", regex = "^(wnml)$")
public class ti_www implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备计时
        Stopwatch sw = Stopwatch.begin();
        // 分析参数
        String phSrc = hc.params.val_check(0);
        String phDist = hc.params.val_check(1);
        boolean transWnml = hc.params.is("wnml");
        boolean quiet = hc.params.is("Q");
        String cdnBase = hc.params.getString("cdn");
        String rs = hc.params.getString("rs", "/gu/rs");
        String base = hc.params.getString("base", "/www/" + sys.getMyGroup() + "/");

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

        // 默认的 cdnBase
        if (Strings.isBlank(cdnBase)) {
            cdnBase = oSrc.getString("cdn_base");
        }

        // 准备标准上下文
        NutMap vars = new NutMap();
        vars.put("rs", rs);
        vars.put("CURRENT_DIR", oWWW.getFormedPath(true));
        vars.put("PAGE_BASE", base);

        // Copy 站点资源
        int mode = Wn.Io.RECUR;
        if (!quiet) {
            mode |= Wn.Io.VERBOSE;
        }
        List<WnObj> tops = sys.io.getChildren(oSrc, null);
        for (WnObj oTop : tops) {
            // site-state.json
            // index.wnml
            if (oTop.isSameName("site-state.json") || oTop.isSameName("index.wnml")) {
                // 这俩稍后再处理
            }
            // 傻傻的 Copy 吧
            else {
                String ph = oWWW.getRegularPath() + oTop.name();
                Wn.Io.copy(sys, mode, oTop, ph);
            }
        }
        sw.tag("CopyResources");

        // Copy site-state.json
        if (!quiet) {
            sys.out.println("Gen site-state.json:");
        }
        doSiteState(sys, quiet, cdnBase, oSrc, oWWW);
        sw.tag("SiteState");

        // 转换 wnml
        WnObj oWnml = sys.io.check(oSrc, "index.wnml");
        doIndexWnml(sys, oSrc, transWnml, quiet, vars, oWWW, oWnml);
        sw.tag("IndexWnml");

        // 结束
        sw.stop();
        if (!quiet) {
            sys.out.println(sw.toString());
        }
    }

    private void doIndexWnml(WnSystem sys,
                             WnObj oSrc,
                             boolean transWnml,
                             boolean quiet,
                             NutMap vars,
                             WnObj oWWW,
                             WnObj oWnml) {
        if (transWnml) {
            if (!quiet) {
                sys.out.println("Gen index.html");
            }
            // 读取文件
            String input = sys.io.readText(oWnml);

            // // 内联 index.js
            // String stub = "<script
            // src=\"${PAGE_BASE}/js/index.js\"></script>";
            // int pos = input.indexOf(stub);
            // if (pos > 0) {
            // WnObj oMainJs = sys.io.check(oSrc, "js/index.js");
            // String rawJs = sys.io.readText(oMainJs);
            // String script = "<script >\n" + rawJs + "\n</script>";
            // String str = input.substring(0, pos);
            // str += script;
            // str += input.substring(pos + stub.length());
            // input = str;
            // }

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
        }
        // 直接 Copy它
        else {
            if (!quiet) {
                sys.out.println("Copy index.wnml");
            }
            WnObj oIndex = sys.io.createIfNoExists(oWWW, "index.wnml", WnRace.FILE);
            Wn.Io.copyFile(sys.io, oWnml, oIndex);
            if (!quiet) {
                sys.out.println(" - done");
            }
        }
    }

    private void doSiteState(WnSystem sys, boolean quiet, String cdnBase, WnObj oSrc, WnObj oDist) {
        WnObj oSiteState = sys.io.check(oSrc, "site-state.json");
        WnObj oTaSS = sys.io.createIfNoExists(oDist, "site-state.json", WnRace.FILE);
        // 无需转换，直接 copy
        if (Strings.isBlank(cdnBase)) {
            Wn.Io.copyFile(sys.io, oSiteState, oTaSS);
            if (!quiet) {
                sys.out.println(" + site-state.json : copied");
            }
        }
        // 增加 cdnBase
        else {
            NutMap map = sys.io.readJson(oSiteState, NutMap.class);
            map.put("cdnBase", cdnBase);
            sys.io.writeJson(oTaSS, map, JsonFormat.full());
            if (!quiet) {
                sys.out.printlnf(" + site-state.json : CDN(%s)", cdnBase);
            }
        }
    }

}
