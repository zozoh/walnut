package org.nutz.walnut.ext.hmaker.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.nutz.lang.Files;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;

/**
 * 进行一次网页转换的上线文
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class HmPageTranslating extends HmContext {

    private static HmComFactory COMs = new HmComFactory();

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
    public NutMap prop;

    /**
     * 当前组件的 arena 区
     */
    public Element eleArena;

    /**
     * 当前处理的 COM 的 ID
     */
    public String comId;

    /**
     * 各个控件有可能生成一些 CSS，都收集在这个容器里，之后会统一在页头生成 style 标签 <br>
     * 这个 Map 的键是控件的 ID，值 NutMap 是控件需要生成的 CSS 规则表
     */
    public Map<String, NutMap> cssRules;

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
        cssRules = new HashMap<String, NutMap>();
        scripts = new LinkedHashSet<String>();
        cssLinks = new LinkedHashSet<String>();
        jsLinks = new LinkedHashSet<String>();
        prop = new NutMap();
    }

    private void __do_com(Element eleCom) {
        this.eleCom = eleCom;

        // 得到控件类型
        String ctype = eleCom.attr("ctype");

        // 处理
        COMs.check(ctype).invoke(this);

        // 移除没必要的属性
        eleCom.removeAttr("ctype");
        eleCom.removeAttr("c_seq");
    }

    private void __do_block(Element eleBlock) {
        // 准备变量
        Element eleCon = eleBlock.child(0);
        Element eleArea = eleCon.child(0);

        // 读取属性
        prop.clear();
        Element eleProp = Hms.fillProp(prop, eleBlock, "hmc-prop-block");
        if (null == eleProp) {
            Element eleCom = eleBlock.select(">.hmb-con>.hmb-area>.hm-com").first();
            throw Er.createf("e.cmd.hmaker.publish.block.noprop",
                             "#%s.%s",
                             eleCom.attr("ctype"),
                             eleCom.attr("id"));
        }
        eleProp.remove();

        // 要挑选的属性
        NutMap cssArea;

        // 对于绝对位置
        if ("abs".equals(prop.getString("mode"))) {

            // 分析
            NutMap cssBlock = new NutMap();

            cssBlock.put("position", "absolute");

            String[] pKeys = prop.getString("posBy", "").split("\\W+");
            String[] pVals = prop.getString("posVal", "").split("[^\\dpx%.-]+");

            if (pKeys.length == pVals.length && pKeys.length > 0) {
                for (int i = 0; i < pKeys.length; i++) {
                    cssBlock.put(pKeys[i], pVals[i]);
                }
            }

            // 设置块属性
            String css = Hms.genCssRuleStyle(this, cssBlock);
            eleBlock.attr("style", css);

            // 生成 Area 部分的 CSS
            cssArea = prop.pick("padding",
                                "border",
                                "borderRadius",
                                "color",
                                "background",
                                "overflow",
                                "boxShadow");
        }
        // 相对位置
        else {
            cssArea = prop.pick("margin",
                                "width",
                                "height",
                                "padding",
                                "border",
                                "borderRadius",
                                "color",
                                "background",
                                "overflow",
                                "boxShadow");
        }

        // 设置 Area 的 CSS
        String css = Hms.genCssRuleStyle(this, cssArea);
        eleArea.attr("style", css);
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
        // 清空页面的头
        doc.head().empty();
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
        this.cssLinks.add("/gu/rs/core/css/font-awesome-4.5.0/css/font-awesome.css");
        this.cssLinks.add("/gu/rs/core/css/font-md/css/material-design-iconic-font.css");
        this.jsLinks.add("/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js");
        this.jsLinks.add("/gu/rs/core/js/backbone/underscore-1.8.2/underscore.js");
        this.jsLinks.add("/gu/rs/core/js/nutz/zutil.js");
        // ---------------------------------------------------
        // 加入皮肤
        if (null != this.oSkinJs) {
            this.jsLinks.add(rootPath + "skin/skin.js");
        }
        if (null != this.oSkinCss) {
            this.cssLinks.add(rootPath + "skin/skin.css");
        }
        // ---------------------------------------------------
        // TODO 处理整个页面的 body
        prop.clear();
        Element eleProp = Hms.fillProp(prop, doc.body(), "hm-page-attr");
        if (null != eleProp) {
            eleProp.remove();
        }
        String css = Hms.genCssRuleStyle(this, prop);
        doc.body().attr("style", css);
        // ---------------------------------------------------
        // 添加页面皮肤过滤器
        if (this.hasSkin()) {
            doc.body().attr("skin", this.skinInfo.name);
        }
        // ---------------------------------------------------
        // 处理块
        Elements eleBlocks = doc.body().getElementsByClass("hm-block");
        for (Element eleBlock : eleBlocks) {
            this.__do_block(eleBlock);
        }
        // ---------------------------------------------------
        // 处理控件
        Elements eleComs = doc.body().getElementsByClass("hm-com");
        for (Element eleCom : eleComs) {
            this.__do_com(eleCom);
        }
        // ---------------------------------------------------
        // 展开链接资源
        __extend_link_and_style();

        // ---------------------------------------------------
        // 准备目标
        // 得到资源的相对路径
        String rph = this.getTargetRelativePath(o);

        // 如果是 wnml 则改变名称
        if (isWnml) {
            rph = Files.renameSuffix(rph, ".wnml");
        }

        // 在目标处创建
        this.oTa = createTarget(rph, o.race());

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
        for (Map.Entry<String, NutMap> en : cssRules.entrySet()) {
            String prefix = "#" + en.getKey();
            NutMap rules = en.getValue();
            String cssText = Hms.genCssText(this, rules, prefix);
            sb.append(cssText);
        }
        if (!Strings.isBlank(sb)) {
            Element eleStyle = doc.head().appendElement("style");
            eleStyle.html(Hms.escapeJsoupHtml(sb.toString()));
        }

        // 在网页尾部链接所有的脚本
        for (String src : jsLinks) {
            Element eleScript = doc.appendElement("script");
            eleScript.attr("type", "text/javascript").attr("src", src);
        }

        // 将所有控件生成 JS 生成对应的 script 标签
        String scriptText = "\n" + Lang.concat("\n", scripts).toString() + "\n";
        if (!Strings.isBlank(scriptText)) {
            Element eleScript = doc.appendElement("script");
            eleScript.attr("type", "text/javascript").text(Hms.escapeJsoupHtml(scriptText));
        }
    }

    // ..............................................................
    // 给控件提供的帮助函数

    /**
     * 为当前控件(由 comId 决定)添加一个 CSS 规则
     * 
     * @param rule
     *            CSS 规则
     */
    public void addMyCss(NutMap rule) {
        NutMap myRule = this.cssRules.get(comId);
        if (null == myRule)
            this.cssRules.put(comId, rule);
        else
            myRule.putAll(rule);
    }

    /**
     * 将给定 link 字符串解释成 HTML 能接受的链接格式。
     * <p>
     * 比如 "id:xxxx" 会被转换成相对路径
     * 
     * @param link
     *            链接
     * @return 转换后的 link
     */
    public String explainLink(String link) {
        // 指向特殊文件
        if (link.startsWith("id:")) {
            WnObj oLink = io.checkById(link.substring(3));
            String rph = this.getRelativePath(this.oSrc, oLink);
            return rph;
        }
        // 其他的，原样返回
        return link;
    }

}
