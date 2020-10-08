package org.nutz.walnut.ext.titanium.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.titanium.www.WnWebsiteTranslating;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnObjWalkjFilter;

@JvmHdlParamArgs(value = "Q", regex = "^wnml$")
public class ti_www implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // .........................................
        // 准备计时
        Stopwatch sw = Stopwatch.begin();

        // .......................................
        // 分析参数
        String phSrc = hc.params.val_check(0);
        String phDist = hc.params.val_check(1);
        boolean transWnml = hc.params.is("wnml");
        String fltRegExp = hc.params.getString("flt");
        boolean fltNot = false;
        if (fltRegExp.startsWith("!")) {
            fltRegExp = fltRegExp.substring(1);
            fltNot = true;
        }

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

        // .......................................
        // 准备服务类
        WnWebsiteTranslating wwt = new WnWebsiteTranslating(sys, oSrc, oWWW);
        wwt.setVars(Lang.map(wnmlSetting));

        // .........................................
        // 准备过滤器
        String s_flt_reg = fltRegExp;
        boolean s_flt_not = fltNot;
        WnObjWalkjFilter flt = new WnObjWalkjFilter() {
            public boolean match(WnObj o) {
                String rph = Wn.Io.getRelativePath(oSrc, o);
                boolean match = !o.isType("scss");
                if (match && null != s_flt_reg) {
                    match = rph.matches(s_flt_reg);
                    if (s_flt_not) {
                        match = !match;
                    }
                }
                if (!quiet) {
                    sys.out.printlnf("   %s %s", match ? "+>" : "~~", rph);
                }
                return match;
            }
        };

        // .........................................
        // Copy 站点资源
        wwt.copyResources(transWnml, flt);
        WnObj oWnml = wwt.getSrcIndexWnmlObj();
        WnObj oSiteState = wwt.getSrcSiteStateObj();
        sw.tag("CopyResources");

        //
        // 要进行转换
        //
        if (transWnml) {
            // .......................................
            // Copy site-state.json
            if (!quiet) {
                sys.out.println("Gen site-state.json:");
            }
            // doSiteState(sys, quiet, vars, oSrc, oWWW, oSiteState);
            wwt.doSiteState(oSiteState);
            sw.tag("SiteState");

            // .......................................
            // 转换 wnml（并同时会记住 index.wnml）
            wwt.doIndexWnml(oWnml);
            sw.tag("IndexWnml");

            // .........................................
            // 如果开启了 -wnml， 实体化的虚页路径
            if (!quiet) {
                sys.out.println("Gen virtual pages:");
            }
            String[] vPages = Strings.splitIgnoreBlank(hc.params.getString("vpages", "page"));
            wwt.doAllVirtualPages(vPages);
        }

        // 结束
        sw.stop();
        if (!quiet) {
            sys.out.println(sw.toString());
        }
    }

}
