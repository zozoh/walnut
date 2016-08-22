package org.nutz.walnut.ext.hmaker.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
     * 当前处理的页面 Document 对象
     */
    public Document doc;

    /**
     * 当前处理的块
     */
    public Element eleBlock;

    /**
     * 当前处理的组件
     */
    public Element eleCom;

    /**
     * 当前处理的组件属性
     */
    public NutMap prop;

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

    public HmPageTranslating(HmContext hpc) {
        super(hpc);
        cssRules = new HashMap<String, NutMap>();
        scripts = new LinkedHashSet<String>();
        cssLinks = new LinkedHashSet<String>();
        jsLinks = new LinkedHashSet<String>();
    }

    private void __do_com() {
        // 得到控件类型
        String ctype = eleCom.attr("ctype");

        // 处理
        COMs.check(ctype).invoke(this);

        // 移除没必要的属性
        eleCom.removeAttr("ctype");
        eleCom.removeAttr("c_seq");
    }

    private void __do_block() {
        // 准备变量
        String mode = null;
        String posBy = null;
        String posVal = null;
        String width = null;
        NutMap posCss = new NutMap();
        NutMap conCss = new NutMap();

        // 属性的前缀
        String prefix = "hmb-";
        int prefixLen = prefix.length();

        // 首先分析所有的属性
        List<String> attNames = new ArrayList<String>();
        Attributes attrs = eleBlock.attributes();
        for (Attribute attr : attrs) {
            String anm = attr.getKey();
            String val = attr.getValue();

            // 块的特殊属性
            if (anm.startsWith(prefix)) {
                // 处理位置。这个属性就不删除了，以便标识绝对位置块等
                if ("hmb-mode".equals(anm)) {
                    mode = val;
                    continue;
                }
                // posBy
                if ("hmb-pos-by".equals(anm)) {
                    posBy = val;
                }
                // posVal
                else if ("hmb-pos-val".equals(anm)) {
                    posVal = val;
                }
                // posBy
                else if ("hmb-width".equals(anm)) {
                    width = val;
                }
                // 其他的计入 CSS
                else {
                    conCss.put(anm.substring(prefixLen), val);
                }

                // 计入特殊属性，稍后准备删除
                attNames.add(anm);
            }
        }

        // 根据位置信息生成 CSS
        // 绝对定位
        if ("abs".equals(mode)) {
            posCss.put("position", "absolute");

            String[] pKeys = Strings.sNull(posBy, "").split("\\W+");
            String[] pVals = Strings.sNull(posVal, "").split("[^\\dpx%.-]+");

            if (pKeys.length == pVals.length && pKeys.length > 0) {
                for (int i = 0; i < pKeys.length; i++) {
                    posCss.put(pKeys[i], pVals[i]);
                }
            }
        }
        // 跟随
        else if ("inflow".equals(mode)) {
            // 厄，啥都没必要做吧
        }
        // 居中
        else if ("center".equals(mode)) {
            posCss.put("margin", "0 auto");
            posCss.put("width", width);
        }

        // 处理位置
        if (posCss.size() > 0) {
            String css = Hms.genCssRuleStyle(posCss);
            eleBlock.attr("style", css);
        }

        // 处理其他样式属性
        if (conCss.size() > 0) {
            String css = Hms.genCssRuleStyle(conCss);
            eleBlock.child(0).attr("style", css);
        }

        // 删除没必要的属性
        for (String anm : attNames)
            eleBlock.removeAttr(anm);
    }

    public WnObj translate(WnObj o) {
        // 记录源
        this.oSrc = o;

        // 准备目标
        this.oTa = this.createTarget(oSrc);

        // 解析页面
        String html = io.readText(oSrc);
        this.doc = Jsoup.parse(html);

        // 清空页面的头
        doc.head().empty();

        // TODO 处理页面的头
        this.cssLinks.add("/gu/rs/ext/hmaker/hm_page.css");
        this.jsLinks.add("/gu/rs/core/js/jquery/jquery-2.1.3/jquery-2.1.3.min.js");
        this.jsLinks.add("/gu/rs/core/js/nutz/zutil.js");

        // TODO 处理整个页面的 body

        // 按块处理
        Elements eleBlocks = doc.body().getElementsByClass("hm-block");
        for (Element eleBlock : eleBlocks) {
            // 处理块
            this.eleBlock = eleBlock;
            this.__do_block();

            // 处理块内的控件
            Elements eleComs = eleBlock.getElementsByClass("hm-com");

            // 虽然假设很多个控件，但是实际上只有一个
            for (Element eleCom : eleComs) {
                this.eleCom = eleCom;
                this.__do_com();
            }
        }

        // 展开链接资源
        __extend_link_and_style();

        // 将处理后的文档写入目标
        html = Hms.unescapeHtmlNewline(doc.html());
        io.writeText(oTa, html);

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
            String cssText = Hms.genCssText(rules, prefix);
            sb.append(cssText);
        }
        if (!Strings.isBlank(sb)) {
            Element eleStyle = doc.head().appendElement("style");
            eleStyle.html(Hms.escapeHtmlNewline(sb.toString()));
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
            eleScript.attr("type", "text/javascript").text(Hms.escapeHtmlNewline(scriptText));
        }
    }

}
