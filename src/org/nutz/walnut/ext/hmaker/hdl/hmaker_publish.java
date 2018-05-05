package org.nutz.walnut.ext.hmaker.hdl;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Each;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Stopwatch;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.log.Log;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.hmaker.skin.HmSkinInfo;
import org.nutz.walnut.ext.hmaker.template.HmTemplate;
import org.nutz.walnut.ext.hmaker.util.HmContext;
import org.nutz.walnut.ext.hmaker.util.HmPageTranslating;
import org.nutz.walnut.ext.hmaker.util.Hms;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("^(quiet|debug|info|warn|keep)$")
public class hmaker_publish implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // ------------------------------------------------------------
        // 上下文
        final HmContext hpc = new HmContext(sys.io, sys.se.group());
        hpc.strict = !hc.params.is("quiet");
        hpc.oConfHome = Wn.checkObj(sys, "~/.hmaker");
        hpc.oApiHome = Wn.getObj(sys, "~/.regapi/api");

        // ------------------------------------------------------------
        // 得到源
        // hpc.oHome = Wn.checkObj(sys, hc.params.val_check(0));
        hpc.oHome = hc.oRefer;

        // ------------------------------------------------------------
        // 得到目标
        String dst = hc.params.val(1);
        // 没有的话，尝试读取源的目标设置
        if (Strings.isBlank(dst)) {
            dst = hpc.oHome.getString("hm_target_release");
        }
        // 还没有!!! 不能忍受，抛错吧
        if (Strings.isBlank(dst)) {
            throw Er.create("cmd.hmaker.publish.nodest");
        }
        hpc.oDest = Wn.checkObj(sys, dst);

        // 源和目标不能相互包含
        if (hpc.oHome.path().startsWith(hpc.oDest.path())
            || hpc.oDest.path().startsWith(hpc.oHome.path())) {
            throw Er.create("e.cmd.hmaker.twine");
        }
        // ------------------------------------------------------------
        // 如果限定了源
        WnObj oSrc = hpc.oHome;
        if (hc.params.has("src")) {
            oSrc = sys.io.check(oSrc, hc.params.get("src"));
            // 确保给定的 site 是 src 的父目录
            if (!oSrc.path().startsWith(hpc.oHome.path())) {
                throw Er.create("e.cmd.hmaker.siteNoIncludeSrc", hpc.oHome + " :: " + oSrc);
            }
        }
        // ------------------------------------------------------------
        // 读取皮肤，如果有的话
        String skinName = hpc.oHome.getString("hm_site_skin");
        if (!Strings.isBlank(skinName)) {
            hpc.oSkinHome = Wn.checkObj(sys, "~/.hmaker/skin/" + skinName);
            hpc.oSkinJs = sys.io.fetch(hpc.oSkinHome, "skin.js");
            hpc.oSkinCss = Hms.genSiteSkinCssObj(sys, hpc.oHome, skinName);
            WnObj oSkinInfo = sys.io.check(hpc.oSkinHome, "skin.info.json");
            hpc.skinInfo = sys.io.readJson(oSkinInfo, HmSkinInfo.class);
        }

        // ------------------------------------------------------------
        // 准备日志输出接口
        Log log = sys.getLog(hc.params);
        Stopwatch sw = Stopwatch.begin();

        // ------------------------------------------------------------
        // 预先分析整个站点，得到页面转换后的扩展名
        hpc.preparePages();
        log.info("preparePages:" + Json.toJson(hpc.pageOutputNames, JsonFormat.full()));

        // ------------------------------------------------------------
        // 准备文件处理逻辑
        Callback<WnObj> callback = new Callback<WnObj>() {
            public void invoke(WnObj o) {
                // 没有后缀，且类型为 "html" 标识着需要转换
                if (Hms.isNeedTranslate(o)) {
                    log.debug(" read: " + o.name());
                    WnObj oTa = new HmPageTranslating(hpc).translate(o);
                    log.infof("   >%s trans ->: %s", hpc.getProcessInfoAndDoCount(), oTa.path());
                }
                // 其他: copy
                else {
                    hpc.resources.add(o);
                }
            }
        };

        // ------------------------------------------------------------
        // 清除源目录
        if (!hc.params.is("keep")) {
            String cmdText = String.format("rm -rfv id:%s/*", hpc.oDest.id());
            log.info("clean dest: " + cmdText);
            sys.exec(cmdText);
        }

        // ------------------------------------------------------------
        // 仅仅处理的是一个文件
        if (oSrc.isFILE()) {
            log.info("do file:");

            // 执行转换
            callback.invoke(oSrc);
        }
        // 要处理的是一个目录
        else {
            log.info("walk in folder:");
            sys.io.walk(oSrc, callback, WalkMode.LEAF_ONLY);
        }
        // ------------------------------------------------------------
        // 将站点用到的模板 copy 过去
        if (!hpc.templates.isEmpty()) {
            log.info("copy template:");
            WnObj oTaTmpl = hpc.createTarget("template", WnRace.DIR);
            for (HmTemplate tmpl : hpc.templates.values()) {
                // 确保删除皮肤文件
                String tmplNm = tmpl.info.name + ".js";
                WnObj oTaTmplJs = sys.io.fetch(oTaTmpl, tmplNm);
                if (null != oTaTmplJs) {
                    sys.io.delete(oTaTmplJs);
                }
                oTaTmplJs = sys.io.create(oTaTmpl, tmplNm, WnRace.FILE);
                log.infof(" +%s %s",
                          hpc.getProcessInfoAndDoCount(),
                          hpc.getRelativeDestPath(oTaTmplJs));
                Wn.Io.copyFile(sys.io, tmpl.oJs, oTaTmplJs);
            }
        }

        // ------------------------------------------------------------
        // 将皮肤目录关键文件 copy 过去
        if (hpc.hasSkin()) {
            // 确保清理皮肤
            if (hc.params.is("keep")) {
                String cmdText = String.format("rm -rfv id:%s/skin", hpc.oDest.id());
                log.info("clean skin: " + cmdText);
                sys.exec(cmdText);
            }

            log.info("copy skin:");
            WnObj oTaSkin = hpc.createTarget("skin", WnRace.DIR);

            // 添加 JS
            WnObj oTaSkinJs = sys.io.createIfNoExists(oTaSkin, "skin.js", WnRace.FILE);
            log.info(" +     add new: " + hpc.getRelativeDestPath(oTaSkinJs));
            Wn.Io.copyFile(sys.io, hpc.oSkinJs, oTaSkinJs);

            // 添加 CSS
            WnObj oTaSkinCss = sys.io.createIfNoExists(oTaSkin, "skin.css", WnRace.FILE);
            log.info(" +     add new: " + hpc.getRelativeDestPath(oTaSkinCss));
            Wn.Io.copyFile(sys.io, hpc.oSkinCss, oTaSkinCss);

            // Copy 其他资源
            sys.io.walk(hpc.oSkinHome, new Callback<WnObj>() {
                public void invoke(WnObj oki) {
                    // 无视所有的 json/less/css 文件
                    if (oki.name().matches("^.*[.](less|css|json|js)$")) {
                        return;
                    }
                    // 来吧 ..
                    String rph = hpc.getRelativePath(hpc.oSkinHome, oki);
                    WnObj oTi = sys.io.createIfNoExists(oTaSkin, rph, WnRace.FILE);
                    log.info(" ++ > " + hpc.getRelativeDestPath(oTi));
                    Wn.Io.copyFile(sys.io, oki, oTi);
                }
            }, WalkMode.LEAF_ONLY);
        }
        // ------------------------------------------------------------
        // 所有依赖的资源: copy 它们
        if (hpc.resources.size() > 0) {
            log.infof("copy %d resources:", hpc.resources.size());

            for (WnObj o : hpc.resources) {
                // 在目标处创建
                WnObj oTa = hpc.createTarget(o);

                // 执行内容的 copy
                Wn.Io.copyFile(sys.io, o, oTa);

                log.infof("  ++%s %s", hpc.getProcessInfoAndDoCount(), oTa.path());
            }
        }
        // 没有需要 copy 的资源，啥也不做
        else {
            log.info("- no resource need to be copy -");
        }
        // ------------------------------------------------------------
        // 输出站点地图文件
        log.info("gen sitemap ...");
        Map<String, NutBean> sitemap = hpc.genSiteMap();
        WnObj oSiteMap = sys.io.createIfNoExists(hpc.oDest, "js/_sitemap.js", WnRace.FILE);
        sys.io.writeText(oSiteMap,
                         String.format("(function(){\nwindow.__SITEMAP = %s;\n})();",
                                       Json.toJson(sitemap, JsonFormat.nice())));
        log.infof(" ... sitemap created : %s", hpc.getRelativeDestPath(oSiteMap));
        // ------------------------------------------------------------
        // 全部输出完成
        sw.stop();
        hpc.markPrcessDone();
        log.infof("%s All done in %dms", hpc.getProcessInfo(false), sw.getDuration());

        // ------------------------------------------------------------
        // 输出目标发布地址
        String page_rph = "";
        // 只有一页 ...
        if (oSrc.isFILE()) {
            String rph = hpc.getRelativePath(oSrc);
            String fnm = hpc.pageOutputNames.get(rph);
            page_rph = "/" + Files.renamePath(rph, fnm);
        }

        // 开始输出吧
        Object wwwObj = hpc.oDest.get("www");
        final String protocol = "http";

        // 如果目标是个 www 目录，顺便也把 `hm_account_set` 和 `hm_role_set` 等属性也弄过去
        if (null != wwwObj) {
            hpc.oDest.put("hm_account_set", hpc.oHome.get("hm_account_set"));
            hpc.oDest.put("hm_role_set", hpc.oHome.get("hm_role_set"));
            hpc.oDest.put("hm_wxmp", hpc.oHome.get("hm_wxmp"));
            sys.io.set(hpc.oDest, "^hm_(account_set|role_set|wxmp)$");
        }

        // 分析配置文件
        NutMap hmConf = hpc.getConf();
        String dftHost = "localhost";
        int port = 80;
        if (null != hmConf) {
            dftHost = hmConf.getString("www_host", "localhost");
            port = hmConf.getInt("www_port", 80);
        }
        String portS = port == 80 ? "" : ":" + port;

        // 目标不是发布目录
        if (null == wwwObj) {
            log.infof("%%[-1/0] ! not www dir: %s", hpc.oDest.path());
        }
        // 目标没有网址，那么采用默认的看看自己的 domain 是什么
        else if ("ROOT".equals(wwwObj)) {
            log.infof("%%[-1/0] %s://%s%s%s", protocol, dftHost, portS, page_rph);
        }
        // 那么一次输出目标地址
        else {
            final String pgurl = page_rph;
            Lang.each(wwwObj, new Each<String>() {
                public void invoke(int index, String host, int length) {
                    log.infof("%%[-1/0] %s://%s%s%s", protocol, host, portS, pgurl);
                }
            });
        }
    }

}
