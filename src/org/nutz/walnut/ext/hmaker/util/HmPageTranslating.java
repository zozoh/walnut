package org.nutz.walnut.ext.hmaker.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;

/**
 * 进行一次网页转换的上线文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class HmPageTranslating extends HmContext {

    private static final Map<String, String> JS_LIB = new HashMap<>();

    static {
        JS_LIB.put("@jquery", "/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js");
        JS_LIB.put("@underscore", "/gu/rs/core/js/backbone/underscore-1.8.2/underscore.js");
        JS_LIB.put("@backbone", "/gu/rs/core/js/backbone/backbone-1.1.2/backbone.js");
        JS_LIB.put("@vue", "/gu/rs/core/js/vue/vue.js");
        JS_LIB.put("@alloy_finger", "/gu/rs/core/js/alloy_finger/alloy_finger.js");
        JS_LIB.put("@zutil", "/gu/rs/core/js/nutz/zutil.js");
        JS_LIB.put("@zdimension", "/gu/rs/core/js/nutz/zdimension.js");
        JS_LIB.put("@dateformat", "/gu/rs/core/js/ui/dateformat.js");
        JS_LIB.put("@seajs", "#seajsnode!/gu/rs/core/js/seajs/seajs-2.3.0/sea.js");
    }

    /**
     * 源文件对象
     */
    public WnObj oSrc;

    /**
     * 目标文件对象
     */
    public WnObj oTa;

    /**
     * 当前文件到根的路径，顶级为 "", 下一级为 "../"
     */
    public String rootPath;

    /**
     * 当前处理的页面 Document 对象
     */
    public Document doc;

    /**
     * 当前处理的组件
     */
    public Element eleCom;

    /**
     * 当前处理的组件属性
     */
    public NutMap propPage;

    /**
     * 当前处理的 COM 的 ID
     */
    public String comId;

    /**
     * 当前处理的 COM 的类型
     */
    public String comType;

    /**
     * 当前处理的组件的布局属性
     */
    public NutMap propBlock;

    /**
     * 当前控件顶级块的 CSS
     */
    public NutMap cssEle;

    /**
     * 当前控件 Arena 的 CSS
     */
    public NutMap cssArena;

    /**
     * 从 propBlock 得出的皮肤属性
     */
    public NutMap skinAttributes;

    /**
     * 当前处理的组件内容属性
     */
    public NutMap propCom;

    /**
     * 各个控件有可能生成一些 CSS，都收集在这个容器里，之后会统一在页头生成 style 标签 <br>
     * 这个 Map 的键是控件的 ID，值 NutMap 是控件需要生成的 CSS 规则表:
     * 
     * <pre>
     * "com0" : {
     *    "selector1" : {..rule..}
     *    "selector2" : {..rule..}
     * },
     * "com0" : {
     *     ..
     * }
     * </pre>
     * 
     * <b>!注意</b> 所有的 "selector" 生成的时候，会被自动添加 "#comId" 前缀
     */
    public Map<String, Map<String, NutMap>> cssRules;

    /**
     * 保存所有的控件生成 JS 代码，以便稍后插入网页末尾
     */
    public LinkedHashSet<String> scripts;

    /**
     * 保存所有的外部引入 CSS，并去除重复
     */
    public LinkedHashSet<String> cssLinks;

    /**
     * 保存所有的外部引入 JavaScript，并去除重复
     */
    public LinkedHashSet<String> jsLinks;

    /**
     * 控件们可以通过 markPageAsWnml() 方法来标记这个页面是 wnml 输出的
     */
    private boolean isWnml;

    public void markPageAsWnml() {
        this.isWnml = true;
    }

    public HmPageTranslating(HmContext hpc) {
        super(hpc);
        cssRules = new HashMap<>();
        scripts = new LinkedHashSet<String>();
        cssLinks = new LinkedHashSet<String>();
        jsLinks = new LinkedHashSet<String>();
    }

    /**
     * @return 一个新分配的一个控件 ID
     */
    private String __assignComId(Element eleCom) {
        String comId = eleCom.id();
        String ctype = eleCom.attr("ctype");

        // 试图向上找到找到自己所在的组件
        Element eleLib = eleCom.parent();
        while (null != eleLib && Strings.isBlank(eleLib.attr("lib"))) {
            eleLib = eleLib.parent();
        }

        // 如果在某个组件内，则需要重新分配
        if (null != eleLib) {
            // 得到所在组件的 ID
            String libId = eleLib.attr("id");

            // 得到自己同类型子控件的前缀
            Pattern regex = Pattern.compile("^" + libId + "_" + ctype + "([0-9]+)$");

            // 寻找所有子控件 ID 的最大值
            int maxId = 0;
            Elements eleSubs = eleLib.select(".hm-com");
            // 二逼 jsoup，select 出来的东西自己先包括上，靠，从 1 开始吧
            for (int i = 1; i < eleSubs.size(); i++) {
                Element eleSub = eleSubs.get(i);
                String subId = eleSub.attr("id");
                Matcher m = regex.matcher(subId);
                if (m.find()) {
                    maxId = Math.max(maxId, Integer.parseInt(m.group(1)));
                }
            }

            // 拼合成新的组件 ID
            comId = libId + "_" + ctype + (maxId + 1);

            // 设置新 ID
            eleCom.attr("id", comId);
        }
        // 否则如果没有 comId 就分配一个新的
        else if (Strings.isBlank(comId)) {
            // 遍历所有同类控件，找到最大的那个 ID 序号
            int seq = 0;
            Pattern regex = Pattern.compile("^" + ctype + "_([\\d]+)$");
            Elements eles = doc.body().select(".hm-com[ctype=\"" + ctype + "\"]");
            for (Element ele : eles) {
                String theId = ele.id();
                Matcher m = regex.matcher(theId);
                if (m.find()) {
                    seq = Math.max(seq, Integer.parseInt(m.group(1)));
                }
            }
            // 嗯，这应该就是新控件的序号
            comId = ctype + "_" + (++seq);
            // 为了保险，再做个判断
            while (null != doc.getElementById(comId)) {
                comId = ctype + '_' + (++seq);
            }
            // 设置新 ID
            eleCom.attr("id", comId);
        }
        // 返回
        return comId;
    }

    /**
     * 如果控件元素代表一个组件，则将其递归展开
     * 
     * @param eleLib
     *            当前控件元素
     * 
     * @return 返回展开后的新元素。如果不是组件，返回输入
     */
    private void __load_lib_and_ensure_comId(Element eleLib) {
        // 确保组件的 ID 被分配
        String theComId = this.__assignComId(eleLib);

        // 判断是否是个组件
        String libName = eleLib.attr("lib");
        if (!Strings.isBlank(libName)) {
            // 读取组件源代码
            String libHtml = this.getLibCode(libName);

            // 将组件内容替换现有元素
            Element eleCom = eleLib.before(libHtml).previousElementSibling();
            eleLib.remove();

            // 设置一下自己的属性
            eleCom.attr("id", theComId).attr("lib", libName);

            // 循环自己所有的子控件
            Elements eleSubComs = eleCom.select(".hm-com");

            // 二逼 jsoup，select 出来的东西自己先包括上，靠，从 1 开始吧
            for (int i = 1; i < eleSubComs.size(); i++) {
                Element eleSubCom = eleSubComs.get(i);
                // 还是组件，嗯，递归解开个小丫挺的
                if (!Strings.isBlank(eleSubCom.attr("lib"))) {
                    this.__load_lib_and_ensure_comId(eleSubCom);
                }
                // 普通控件的话，确保有 ID
                else {
                    this.__assignComId(eleSubCom);
                }
            }
        }
    }

    private void __do_com(Element eleCom) {
        // 将元素记录到上下文
        this.eleCom = eleCom;
        this.comId = eleCom.attr("id");

        // 得到控件类型
        this.comType = eleCom.attr("ctype");

        // 处理
        Hms.COMs.check(this.comType).invoke(this);

        // 移除没必要的属性
        eleCom.removeAttr("c_seq");
        eleCom.removeAttr("hmc-mode");
        eleCom.removeAttr("hmc-pos-by");
        eleCom.removeAttr("hm-actived");
        eleCom.removeAttr("ui-id");
    }

    public WnObj translate(WnObj o) {
        // 记录源
        this.oSrc = o;
        // ---------------------------------------------------
        // 计算源到根的路径
        this.rootPath = this.getRelativePath(oSrc, oHome);
        // ---------------------------------------------------
        // 解析页面
        String html = io.readText(oSrc);
        this.doc = Jsoup.parse(html);
        // ---------------------------------------------------
        // 标识一下运行环境
        this.doc.body().parent().attr("hmaker-runtime", "yes");
        // ---------------------------------------------------
        // 清空页面的头
        doc.head().empty();
        // ---------------------------------------------------
        // 添加页面标题
        doc.head().appendElement("title").text(this.oSrc.getString("title", this.oSrc.name()));
        // ---------------------------------------------------
        // 删除自己和下属所有节点的 style 属性
        doc.body().getElementsByAttribute("style").removeAttr("style");
        // ---------------------------------------------------
        // 添加必要的元数据
        doc.head().append("<meta charset=\"utf-8\">");
        doc.head()
           .append("<meta name=\"viewport\""
                   + " content=\"width=device-width, "
                   + "initial-scale=1.0, "
                   + "user-scalable=0, "
                   + "minimum-scale=1.0, "
                   + "maximum-scale=1.0\">");
        doc.head().append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">");
        // ---------------------------------------------------
        // TODO 处理页面的头
        this.cssLinks.add("/gu/rs/ext/hmaker/hm_page.css");
        this.cssLinks.add("/gu/rs/core/css/font-awesome/css/font-awesome.css");
        this.cssLinks.add("/gu/rs/core/css/font-md/css/material-design-iconic-font.css");
        this.jsLinks.add("/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js");
        this.jsLinks.add("/gu/rs/core/js/backbone/underscore-1.8.2/underscore.js");
        this.jsLinks.add("#seajsnode!/gu/rs/core/js/seajs/seajs-2.3.0/sea.js");
        this.jsLinks.add("/gu/rs/core/js/nutz/zutil.js");
        this.jsLinks.add("/gu/rs/core/js/nutz/zdimension.js");
        this.jsLinks.add("/gu/rs/core/js/ui/dateformat.js");
        this.jsLinks.add("/gu/rs/ext/hmaker/hm_runtime.js");
        // ---------------------------------------------------
        // 加入皮肤
        if (null != this.oSkinJs) {
            // 处理皮肤的依赖库
            if (null != this.skinInfo.js) {
                for (String jslib : this.skinInfo.js) {
                    String libph = JS_LIB.get(jslib);
                    this.jsLinks.add(libph);
                }
            }

            // 链接皮肤文件
            this.jsLinks.add(rootPath + "skin/skin.js");

            // 启用皮肤的脚本
            this.jsLinks.add("/gu/rs/ext/hmaker/skin_main.js");

            // 给皮肤脚本设置一个皮肤的相对路径，以便加载器加载
            String skinRph = "./" + rootPath + "skin/skin.js";
            this.addScript("window.skin_js_path='%s';", skinRph);
        }
        if (null != this.oSkinCss) {
            this.cssLinks.add(rootPath + "skin/skin.css");
        }
        // ---------------------------------------------------
        // TODO 处理整个页面的 body
        this.propPage = Hms.loadPropAndRemoveNode(doc.body(), "hm-page-attr");
        doc.body().removeAttr("assisted-off").removeAttr("assisted-on");
        // ---------------------------------------------------
        // 添加页面皮肤过滤器
        if (this.hasSkin()) {
            doc.body().attr("skin", this.skinInfo.name);
        }
        // ---------------------------------------------------
        // 首先递归展开全部组件
        Elements eleLibs = doc.body().select(".hm-com[lib]");
        for (Element eleLib : eleLibs) {
            this.__load_lib_and_ensure_comId(eleLib);
        }
        // ---------------------------------------------------
        // 依次处理控件
        // 考虑到 dynamic 组件需要获取其他控件的 getValue，因此要延迟一下
        Elements eleComs = doc.body().getElementsByClass("hm-com");
        List<Element> eleDelayList = new ArrayList<>(eleComs.size());
        for (Element eleCom : eleComs) {
            // 标识自己是运行时
            eleCom.attr("hmaker-rt", "yes");

            // dynamic 控件需要延迟
            if ("dynamic".equals(eleCom.attr("ctype"))) {
                eleDelayList.add(eleCom);
                continue;
            }

            // 执行自己的控件渲染逻辑
            this.__do_com(eleCom);
        }
        // 处理延迟转换的控件
        for (Element eleCom : eleDelayList) {
            this.__do_com(eleCom);
        }

        // ---------------------------------------------------
        // 对于所有的 <img> 标签，处理一遍 src
        Elements eleImgs = doc.body().getElementsByTag("img");
        for (Element eleImg : eleImgs) {
            String src = eleImg.attr("src");
            src = this.explainLink(src, true);
            if (null != src)
                eleImg.attr("src", src);
        }
        // ---------------------------------------------------
        // 对于所有的 <a> 标签，处理一遍 href
        Elements eleAnchors = doc.body().getElementsByTag("a");
        for (Element eleAnchor : eleAnchors) {
            String href = eleAnchor.attr("href");
            href = this.explainLink(href, false);
            if (null != href)
                eleAnchor.attr("href", href);
        }
        // ---------------------------------------------------
        // 如果控件们决定当前页是一个 wnml，那么增加一个动态占位符，用来为 JS 脚本们
        // 输出本页的请求参数
        if (this.isWnml) {
            this.scripts.add("window.__REQUEST = ${params(json:cqn)?-obj-};");
        }
        // ---------------------------------------------------
        // 展开链接资源
        __extend_link_and_style();

        // ---------------------------------------------------
        // 最后把页面直接链接的资源也加入 head
        List<NutMap> list = this.propPage.getList("links", NutMap.class);
        this.propPage.remove("links");
        if (null != list && list.size() > 0) {
            for (NutMap link : list) {
                WnObj oLink = this.io.fetch(null, link.getString("ph"));
                if (null != oLink) {
                    String rph = this.getRelativePath(oSrc, oLink);
                    Element eleLink = doc.head().appendElement("link");
                    eleLink.attr("rel", "stylesheet").attr("type", "text/css").attr("href", rph);
                }
            }
        }

        // Body 的 style
        String css = Hms.genCssRuleStyle(this, propPage);
        if (null != css) {
            doc.body().attr("style", css);
        }

        // ---------------------------------------------------
        // 准备目标
        // 得到资源的相对路径
        String rph = this.getTargetRelativePath(o) + ".html";

        // 在目标处创建
        this.oTa = createTarget(rph, o.race());

        // 标识目标为动态处理
        if (this.isWnml) {
            this.oTa.setv("as_wnml", true);
            io.set(this.oTa, "^as_wnml$");
        }
        // ---------------------------------------------------
        // 将处理后的文档写入目标
        html = Hms.unescapeJsoupHtml(doc.html());
        io.writeText(oTa, html);
        // ---------------------------------------------------
        // 返回创建结果文件
        return oTa;
    }

    private void __extend_link_and_style() {
        // 在网页头部链接所有的 CSS
        for (String href : cssLinks) {
            Element eleLink = doc.head().appendElement("link");
            eleLink.attr("rel", "stylesheet").attr("type", "text/css").attr("href", href);
        }

        // 将所有控件生成的 CSS 生成一个 style 标签插入页头
        StringBuilder sb = new StringBuilder("\n");
        for (Map.Entry<String, Map<String, NutMap>> en : cssRules.entrySet()) {
            String prefix = "#" + en.getKey();
            Map<String, NutMap> rules = en.getValue();
            if (rules.size() > 0) {
                String cssText = Hms.genCssText(this, rules, prefix);
                if (!Strings.isBlank(cssText))
                    sb.append(cssText);
            }
        }
        if (!Strings.isBlank(sb)) {
            Element eleStyle = doc.head().appendElement("style");
            eleStyle.html(Hms.escapeJsoupHtml(sb.toString()));
        }

        // 链接所有的脚本
        Pattern IDP = Pattern.compile("^#(\\w+)!(.+)$");
        Element eleScript;
        for (String src : jsLinks) {
            Matcher m = IDP.matcher(src);
            // 声明了 ID 的脚本
            if (m.find()) {
                String scriptId = m.group(1);
                String scriptSrc = m.group(2);
                eleScript = doc.head()
                               .appendElement("script")
                               .attr("src", scriptSrc)
                               .attr("id", scriptId);
            }
            // 其他脚本
            else {
                eleScript = doc.head().appendElement("script");
                eleScript.attr("src", src);
            }
            // 设定脚本类型
            eleScript.attr("type", "text/javascript");
        }

        // 将所有控件生成 JS 生成对应的 script 标签
        String scriptText = "\n" + Lang.concat("\n", scripts).toString() + "\n";
        if (!Strings.isBlank(scriptText)) {
            eleScript = doc.appendElement("script");
            eleScript.attr("type", "text/javascript").text(Hms.escapeJsoupHtml(scriptText));
        }
    }

    // ..............................................................
    // 给控件提供的帮助函数
    /**
     * 为当前控件添加一个规则
     * 
     * @param selector
     *            选择器（会被添加 #comdId)
     * @param rule
     *            规则
     * 
     * @see #cssRules
     */
    public void addMyRule(String selector, NutMap rule) {
        // 没规则，无视吧
        if (null == rule || rule.isEmpty())
            return;

        // 先看看本控件有木有规则集合 ...
        Map<String, NutMap> myRules = this.cssRules.get(comId);

        // 木有，为控件建立一个新的规则集
        if (null == myRules) {
            Map<String, NutMap> rules = new HashMap<>();
            rules.put(selector, rule);
            this.cssRules.put(comId, rules);
        }
        // 有规则集合，找找看是否需要融合
        else {
            NutMap rules = myRules.get(selector);
            // 新建
            if (null == rules) {
                myRules.put(selector, rule);
            }
            // 融合
            else {
                rules.mergeWith(rule);
            }
        }
    }

    /**
     * 为当前控件添加一个规则，如果当前组件有皮肤，则附加皮肤类选择器的前缀
     * 
     * @param selector
     *            选择器（会被添加 #comdId)
     * @param rule
     *            规则
     * 
     * @see #addMyRule(String, NutMap)
     */
    public void addMySkinRule(String selector, NutMap rule) {
        String skin = this.eleCom.attr("skin");
        if (!Strings.isBlank(skin) && null != selector) {
            String[] ss = Strings.splitIgnoreBlank(selector);
            for (int i = 0; i < ss.length; i++) {
                ss[i] = "." + skin + " " + ss[i];
            }
            selector = Lang.concat(",", ss).toString();
        }
        this.addMyRule(selector, rule);
    }

    /**
     * 增加一段 Javascript
     * 
     * @param script
     *            脚本代码
     */
    public void addScriptOnLoad(String script) {
        this.scripts.add(Hms.wrapjQueryDocumentOnLoad(script));
    }

    public void addScriptOnLoadf(String fmt, Object... args) {
        addScriptOnLoad(String.format(fmt, args));
    }

    public void addScript(String fmt, Object... args) {
        this.scripts.add(String.format(fmt, args));
    }

    /**
     * 将给定 link 字符串解释成 HTML 能接受的链接格式。
     * <p>
     * 比如 "id:xxxx" 会被转换成相对路径
     * 
     * @param link
     *            链接
     * @param asResource
     *            表示本链接为资源，需要加入资源列表（比如 image.src 就是这个情况）
     * @return 转换后的 link， null 表示链接目标不存在
     */
    public String explainLink(String link, boolean asResource) {
        if (Strings.isBlank(link))
            return null;

        // 一个绝对链接
        if (link.matches("^(https?://|javascript:).+$")) {
            return link;
        }

        // 分析链接是否包含参数
        int pos = link.indexOf('?');
        String params = null;
        if (pos > 0) {
            params = link.substring(pos);
            link = link.substring(0, pos);
        }

        // 就是指向一个文件咯
        WnObj oLink;

        // 指向特殊文件
        if (link.startsWith("id:")) {
            oLink = io.get(link.substring(3));
        }
        // 相对于站点的绝对链接
        else if (link.startsWith("/")) {
            oLink = io.fetch(this.oHome, link.substring(1));
        }
        // 被认为是相对链接，那么试图找到这个文件
        else {
            oLink = io.fetch(this.oSrc, link);
        }

        // 如果链接目标不存在，返回空
        if (null == oLink)
            return null;

        // 计算相对路径
        String rph = this.getRelativePath(this.oSrc, oLink);

        // 需要计入资源
        if (asResource) {
            this.resources.add(oLink);
            return rph;
        }

        // 返回相对链接对应的页面
        String pageName = this.pageOutputNames.get(rph);
        if (!Strings.isBlank(pageName))
            rph = Files.renamePath(rph, pageName);

        // 返回
        return null == params ? rph : rph + params;
    }

}
