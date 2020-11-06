package org.nutz.walnut.ext.titanium.www;

import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WalkMode;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.www.JvmWnmlRuntime;
import org.nutz.walnut.ext.www.WnmlRuntime;
import org.nutz.walnut.ext.www.WnmlService;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnObjWalkjFilter;

public class WnWebsiteTranslating {

    private WnSystem sys;

    private WnmlRuntime wnmlRuntime;

    private WnmlService wnmlService;

    private WnObj srcDirObj;

    private WnObj wwwDirObj;

    private boolean quiet;

    private NutMap vars;

    private WnObj srcAppJsonObj;

    private WnObj srcIndexWnmlObj;

    private WnObj srcSiteStateObj;

    private WebsiteState siteState;

    private NutMap siteStateMap;

    private WnObj wwwIndexWnmlObj;

    private Document wwwIndexDoc;

    public WnWebsiteTranslating(WnSystem sys, WnObj oSrc, WnObj oWWW) {
        this.wnmlRuntime = new JvmWnmlRuntime(sys);
        this.wnmlService = new WnmlService();
        this.sys = sys;
        this.srcDirObj = oSrc;
        this.wwwDirObj = oWWW;
    }

    public List<WnObj> copyResources(boolean transWnml, WnObjWalkjFilter flt) {
        List<WnObj> tops = sys.io.getChildren(srcDirObj, null);
        for (WnObj oTop : tops) {
            // 要转换？嗯， _app.json 稍后再处理吧
            if (transWnml && oTop.isSameName("_app.json")) {
                this.srcAppJsonObj = oTop;
            }
            // 要转换？ 嗯， site-state.json 稍后再处理吧
            else if (transWnml && oTop.isSameName("site-state.json")) {
                this.srcSiteStateObj = oTop;
            }
            // 要转换？ 嗯， index.wnml 稍后再处理吧
            else if (transWnml && oTop.isSameName("index.wnml")) {
                this.srcIndexWnmlObj = oTop;
            }
            // 傻傻的 Copy 吧
            else if (flt.match(oTop)) {
                String ph = wwwDirObj.getRegularPath();
                Wn.Io.copy(sys, Wn.Io.RECUR, oTop, ph, flt);
            }
        }
        return tops;
    }

    public void doAllVirtualPages(String[] vPages) {
        if (null == vPages || null == this.wwwIndexWnmlObj)
            return;
        for (String vPage : vPages) {
            if (!quiet) {
                sys.out.printf("@%s:\n", vPage);
            }
            WnObj oPageDir = sys.io.fetch(wwwDirObj, vPage);
            if (null != oPageDir && oPageDir.isDIR()) {
                sys.io.walk(oPageDir, (oJson) -> {
                    if (!oJson.isType("json")) {
                        return;
                    }
                    doVirtualPage(oPageDir, oJson);
                }, WalkMode.LEAF_ONLY);
            }
        }
    }

    public void doVirtualPage(WnObj oPageDir, WnObj oJson) {
        // .......................................
        // 准备输出文件的名称
        String name = Files.getMajorName(oJson.name()) + ".html";
        if (!quiet) {
            String rph = Wn.Io.getRelativePath(oPageDir, oJson);
            sys.out.printf("  -> %s => %s\n", rph, name);
        }

        // .......................................
        WnObj oPage = sys.io.createIfNoExists(oJson.parent(), name, WnRace.FILE);

        // .......................................
        // 准备转换上下文
        NutMap wnmlContext = getWnmlContext();

        // .......................................
        // 搞个新的并渲染
        Document doc = wwwIndexDoc.clone();
        this.wnmlService.invoke(this.wnmlRuntime, wnmlContext, doc);

        // .......................................
        // 准备服务器端渲染
        // .......................................
        // 写入虚页内容
        String json = sys.io.readText(oJson);
        appendSSRResult(doc, "page-json", null, json);

        // 这里寻找所有的可以预先被渲染的 api
        if (null != this.siteState) {
            WebsitePage page = Json.fromJson(WebsitePage.class, json);
            Map<String, WebsiteApi> apis = page.getPreloaSsrdApi(siteState);

            // 依次调用每个 api 的接口
            NutMap apiContext = new NutMap();
            for (Map.Entry<String, WebsiteApi> en : apis.entrySet()) {
                String key = en.getKey();
                WebsiteApi api = en.getValue();
                api.explainParams(apiContext);

                // 准备参数
                String qsJson = api.getParamsValueJson();
                String qsFinger = Lang.sha1(qsJson);

                // 准备命令
                String cmdText = "httpapi invoke " + api.getPath() + " -get @pipe";
                String apiRe = sys.exec2(cmdText, qsJson);

                // 计入输出结果
                appendSSRResult(doc, "api-" + key, qsFinger, apiRe);
            }
        }

        // 写入
        String html = doc.toString();
        sys.io.writeText(oPage, html);
    }

    private void appendSSRResult(Document doc, String ssrKey, String ssrFinger, String json) {
        Element $tmpl = doc.createElement("div");
        $tmpl.addClass("wn-ssr-data");
        $tmpl.attr("data-ssr-key", ssrKey);
        if (null != ssrFinger) {
            $tmpl.attr("data-ssr-finger", ssrFinger);
        }
        $tmpl.appendText(json);
        doc.body().prependChild($tmpl);
    }

    public void doIndexWnml(WnObj oWnml) {
        if (!quiet) {
            sys.out.println("Gen index.html");
        }
        // 读取文件
        String input = sys.io.readText(oWnml);

        // 准备转换上下文
        NutMap context = getWnmlContext();

        // 执行转换
        this.wwwIndexWnmlObj = sys.io.createIfNoExists(wwwDirObj, "index.html", WnRace.FILE);
        this.wwwIndexDoc = Jsoup.parse(input);

        // 搞个新的并渲染
        Document doc = wwwIndexDoc;
        this.wnmlService.invoke(this.wnmlRuntime, context, doc);

        // 寻找到入口函数
        Elements $mains = doc.select("script[ssr-page-main]");
        if (null != $mains && $mains.size() > 0) {
            Element $main = $mains.get(0);
            String mainJs = $main.html();
            // 生成一个新的全局入口 JS
            WnObj oMainJs = sys.io.createIfNoExists(wwwDirObj, "main.js", WnRace.FILE);
            sys.io.writeText(oMainJs, mainJs);
            // 不需要了
            $mains.remove();

            // 获取 pageBase
            String base = context.getString("base", "/");

            // 添加一个引入
            $main = doc.createElement("script");
            $main.attr("type", "module");
            $main.attr("src", Wn.appendPath(base, "main.js"));
            doc.body().appendChild($main);
        }

        // 输出
        String html = doc.toString();
        sys.io.writeText(this.wwwIndexWnmlObj, html);

        if (!this.quiet) {
            sys.out.println(" - done");
        }

    }

    private NutMap getWnmlContext() {
        NutMap context = new NutMap();
        String rootPath = wwwDirObj.path();

        context.put("WWW", wwwDirObj.pickBy("^(id|hm_.+)$"));
        context.put("SITE_HOME", rootPath);
        context.put("grp", sys.getMyGroup());
        context.putAll(this.vars);
        return context;
    }

    public void doSiteState(WnObj oSiteState) {
        WnObj oTaSS = sys.io.createIfNoExists(wwwDirObj, "site-state.json", WnRace.FILE);
        String text = sys.io.readText(oSiteState);
        this.siteStateMap = Json.fromJson(NutMap.class, text);
        // 因为正在 ti-web-app-main.mjs 这个入口层面增加了 deps
        // 这里就不要增加了
        siteStateMap.putAll(this.vars);
        siteStateMap.remove("deps");

        // 解析
        this.siteState = Lang.map2Object(siteStateMap, WebsiteState.class);

        sys.io.writeJson(oTaSS, siteStateMap, JsonFormat.full());
        if (!this.quiet) {
            JsonFormat jfmt = JsonFormat.compact().setQuoteName(false);
            String brief = Json.toJson(vars, jfmt);
            sys.out.printlnf(" + site-state.json : {%s}", brief);
        }
    }

    public void doAppJson(WnObj oAppJson) {
        WnObj oTaApp = sys.io.createIfNoExists(wwwDirObj, "_app.json", WnRace.FILE);
        NutMap app = sys.io.readJson(oAppJson, NutMap.class);
        NutMap store = app.getAs("store", NutMap.class);
        if (null != store) {
            store.put("state", this.siteStateMap);
            app.put("store", store);
        }
        sys.io.writeJson(oTaApp, app, JsonFormat.full());
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public NutMap getVars() {
        return vars;
    }

    public void setVars(NutMap vars) {
        this.vars = vars;
        this.vars.put("CURRENT_DIR", wwwDirObj.getFormedPath(true));
        this.vars.put("PAGE_BASE", this.vars.get("pageBase"));
    }

    public WnSystem getSys() {
        return sys;
    }

    public void setSys(WnSystem sys) {
        this.sys = sys;
    }

    public WnmlRuntime getWnmlRuntime() {
        return wnmlRuntime;
    }

    public void setWnmlRuntime(WnmlRuntime wrt) {
        this.wnmlRuntime = wrt;
    }

    public WnmlService getWnmlService() {
        return wnmlService;
    }

    public void setWnmlService(WnmlService ws) {
        this.wnmlService = ws;
    }

    public WnObj getSrcDirObj() {
        return srcDirObj;
    }

    public void setSrcDirObj(WnObj oSrc) {
        this.srcDirObj = oSrc;
    }

    public WnObj getWwwDirObj() {
        return wwwDirObj;
    }

    public void setWwwDirObj(WnObj oWWW) {
        this.wwwDirObj = oWWW;
    }

    public WnObj getSrcIndexWnmlObj() {
        return srcIndexWnmlObj;
    }

    public void setSrcIndexWnmlObj(WnObj oSrcIndexWnml) {
        this.srcIndexWnmlObj = oSrcIndexWnml;
    }

    public WnObj getSrcSiteStateObj() {
        return srcSiteStateObj;
    }

    public WnObj getSrcAppJsonObj() {
        return srcAppJsonObj;
    }

    public void setSrcAppJsonObj(WnObj srcAppJsonObj) {
        this.srcAppJsonObj = srcAppJsonObj;
    }

    public void setSrcSiteStateObj(WnObj oSrcSiteState) {
        this.srcSiteStateObj = oSrcSiteState;
    }

    public WnObj getWwwIndexWnmlObj() {
        return wwwIndexWnmlObj;
    }

    public void setWwwIndexWnmlObj(WnObj oWWWIndexWnml) {
        this.wwwIndexWnmlObj = oWWWIndexWnml;
    }

    public Document getWwwIndexDoc() {
        return wwwIndexDoc;
    }

    public void setWwwIndexDoc(Document docWWWIndexWnml) {
        this.wwwIndexDoc = docWWWIndexWnml;
    }

}
