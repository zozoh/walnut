package com.site0.walnut.cheap.markdown;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.site0.walnut.util.Wlang;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import com.site0.walnut.cheap.dom.CheapComment;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapNode;
import com.site0.walnut.util.Wcol;
import com.site0.walnut.util.Ws;

public class CheapMarkdownParsing {

    private static final Map<LineType, ParseBlock> parser = new HashMap<>();

    static {
        parser.put(LineType.PARAGRAPH, new ParseBlockAsParagraph());
        parser.put(LineType.HEADING, new ParseBlockAsHeading());
        parser.put(LineType.BLOCKQUOTE, new ParseBlockAsBlockquote());
        parser.put(LineType.LIST, new ParseBlockAsList());
        parser.put(LineType.CODE_BLOCK, new ParseBlockAsCodeBlock());
        parser.put(LineType.HR, new ParseBlockAsHr());
        parser.put(LineType.TABLE, new ParseBlockAsTable());
        parser.put(LineType.BLANK, new ParseBlockAsBlank());
        parser.put(LineType.LINK_REFER, new ParseBlockAsLinkRefer());
    }

    /**
     * 文档体元素
     */
    CheapDocument doc;

    /**
     * 当前解析元素
     */
    CheapNode $current;

    /**
     * 每行增加一个 BR 元素
     */
    boolean autoBr;

    /**
     * 对于普通段落，不要输出 P标签
     */
    boolean unwrapParagraph;

    /**
     * 扫描完毕的块
     */
    private LinkedList<CheapBlock> blocks;

    CheapBlockParsing BP;

    public CheapMarkdownParsing clone() {
        CheapMarkdownParsing ing = new CheapMarkdownParsing(doc, $current);
        ing.BP = this.BP.clone();
        return ing;
    }

    private CheapMarkdownParsing(CheapDocument doc, CheapNode $current) {
        this.doc = doc;
        this.$current = $current;
    }

    public CheapMarkdownParsing() {
        this(true, 4, "html", "body", null);
    }

    public CheapMarkdownParsing(boolean autoBr) {
        this(autoBr, 4, "html", "body", null);
    }

    public CheapMarkdownParsing(String wrapTagName) {
        this(true, 4, "markdown", "body", wrapTagName);
    }

    public CheapMarkdownParsing(boolean autoBr, String wrapTagName) {
        this(autoBr, 4, "html", "body", wrapTagName);
    }

    public CheapMarkdownParsing(boolean autoBr,
                                int tabWidth,
                                String rootTagName,
                                String bodyTagName,
                                String wrapTagName) {
        this.doc = new CheapDocument(rootTagName, null, bodyTagName);
        this.doc.setAutoClosedTagsAsHtml();
        if (null != wrapTagName) {
            CheapElement $wrap = this.doc.createElement(wrapTagName, "as-md");
            this.$current = $wrap.appendTo(doc.body());
        } else {
            this.$current = this.doc.body();
        }
        this.autoBr = autoBr;
        this.BP = new CheapBlockParsing(tabWidth, bodyTagName);
    }

    void processFont(String input) {
        int pos = 0;
        Matcher m = Pfont.matcher(input);
        // 循环匹配项
        while (m.find()) {
            // 位置
            int s = m.start();

            // 前置内容
            if (s > pos) {
                String text = input.substring(pos, s);
                doc.createTextNode(text).appendTo($current);
            }

            // 匹配字体相关
            pos = __process_font_matcher(m);

            // 这个分支打破了我的世界观
            if (-1 == pos) {
                throw Wlang.impossible();
            }
        }
        // 尾部
        if (pos < input.length()) {
            String text = input.substring(pos);
            doc.createTextNode(text).appendTo($current);
        }
    }

    private CheapElement processFontElement(Matcher m, String tagName, int groupIndex) {
        CheapElement $el = doc.createElement(tagName, "as-md");
        $el.appendTo($current);
        this.$current = $el;
        String str = m.group(groupIndex);
        processFont(str);
        this.$current = $el.parentElement();
        return $el;
    }

    private static String REGEX_Font = "(" // Start: 1
                                       + "([*]{2}([^*]+)[*]{2})" // STRONG:2,3
                                       + "|([*]([^*]+)[*])" // EM:4,5
                                       + "|(__([^_]+)__)" // B:6,7
                                       + "|(_([^_]+)_)" // I:8,9
                                       + "|(~~([^~]+)~~)" // Del:10,11
                                       + ")";
    private static Pattern Pfont = Regex.getPattern(REGEX_Font);

    private int __process_font_matcher(Matcher m) {
        // BOLD:2,3
        // "([*]{2}([^*]+)[*]{2})"
        if (null != m.group(2)) {
            processFontElement(m, "b", 3);
        }
        // I:4,5
        // "|([*]([^*]+)[*])"
        else if (null != m.group(4)) {
            processFontElement(m, "i", 5);
        }
        // STRONG:6,7
        // "|(__([^_]+)__)"
        else if (null != m.group(6)) {
            processFontElement(m, "strong", 7);
        }
        // EM:8,9
        // "|(_([^_]+)_)"
        else if (null != m.group(8)) {
            processFontElement(m, "em", 9);
        }
        // Del:10,11
        // "|(~~([^~]+)~~)"
        else if (null != m.group(10)) {
            processFontElement(m, "del", 11);
        }
        // 没有我可以处理的匹配项
        else {
            return -1;
        }
        // 偏移当前下标
        return m.end();
    }

    void parseLine(CheapLine line) {
        parseLine(line.content);
    }

    private static String REGEX = "(" // Start: 1
                                  + "([*]{2}([^*]+)[*]{2})" // STRONG:2,3
                                  + "|([*]([^*]+)[*])" // EM:4,5
                                  + "|(__([^_]+)__)" // B:6,7
                                  + "|(_([^_]+)_)" // I:8,9
                                  + "|(~~([^~]+)~~)" // Del:10,11
                                  + "|(`([^`]+)`)" // Code:12,13
                                  // Link:14,15(T),16(Href),17,18(Alt)
                                  + "|(\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
                                  // Refer Link:19,20(T),21(Refer)
                                  + "|(\\[(.*)\\]\\[([^\\]]+)\\])"
                                  // Image:22,23(T),24(Src),25,26(Alt)
                                  + "|(!\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
                                  // HTML Tag begin: 27,28(Name),29,30(Attrs)
                                  + "|(<([a-z1-6]+)(\\s([^>]+))?>)"
                                  // HTML Tag end: 31,32(name)
                                  + "|(</([a-z1-6]+)>)"
                                  // HTML Comment begin: 33
                                  + "|(<!--)"
                                  + ")";
    private static Pattern P = Regex.getPattern(REGEX);

    void parseLine(String input) {
        // 防守
        if (null == input)
            return;

        // 起始位置
        int pos = 0;

        //
        // 如果前行是注释，先尝试结束注释
        //
        if ($current.isComment()) {
            int p2 = input.indexOf("-->");
            // 尝试失败，统统加入注释行!
            if (p2 < 0) {
                ((CheapComment) $current).appendLine(input);
                return;
            }
            // 结束注释
            String text = input.substring(0, p2);
            ((CheapComment) $current).appendLine(text);
            $current = $current.getParent();
            // 重置输入内容
            input = input.substring(p2 + 3);
        }

        //
        // 开始解析行
        //
        Matcher m = P.matcher(input);
        while (m.find(pos)) {
            // 位置
            int s = m.start();

            // 前置内容
            if (s > pos) {
                String text = input.substring(pos, s);
                doc.createTextNode(text).appendTo($current);
            }

            // B/I/STRONG/EM/DEL
            int e = __process_font_matcher(m);

            // 处理的字体的匹配，后面的就不用继续看了
            if (e >= 0) {
                pos = e;
                continue;
            }
            // 那么就更新一下结束位置
            else {
                e = m.end();
            }

            //
            // 开始判断 ...
            //

            // Code:12,13
            // "|(`([^`]+)`)"
            if (null != m.group(12)) {
                String str = m.group(13);
                CheapElement $el = doc.createElement("code", "as-md");
                $el.appendTo($current).appendText(str);
            }
            // Link:14,15(T),16(Href),17,18(Alt)
            // "|(\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
            else if (null != m.group(14)) {
                processLinkElement(m);
            }
            // Refer Link:19,20(T),21(Refer)
            // "|(\\[(.*)\\]\\[([^\\]]+)\\])"
            else if (null != m.group(19)) {
                processLinkReferElement(m);
            }
            // Image:22,23(T),24(Src),25,26(Alt)
            // "|(!\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
            else if (null != m.group(22)) {
                processImgElement(m);
            }
            // HTML Tag begin: 27,28(Name),29,30(Attrs)
            // "|(<([a-z1-6]+)(\\s([^>]+))?>)"
            else if (null != m.group(27)) {
                String tagName = m.group(28);
                CheapElement $el = doc.createElement(tagName);
                $el.appendTo($current);
                // 解析属性
                String attrs = m.group(30);
                if (null != attrs) {
                    NutMap bean = Ws.splitAttrMap(attrs);
                    $el.setAttrs(bean);
                }

                // 不能直接结束的标签，需要压栈
                if (!doc.isAutoClosedTag($el)) {
                    $current = $el;
                }
            }
            // HTML Tag end: 31,32(name)
            // "|(</([a-z1-6]+)>)"
            else if (null != m.group(31)) {
                String tagName = m.group(32).toUpperCase();
                CheapElement $el = (CheapElement) $current;
                // 当前就是
                if ($el.isStdTagName(tagName)) {
                    $current = $el.getParent();
                }
                // 向上查找
                else {
                    $el = $current.getClosestByTagName(tagName);
                    // 木有找到可以闭合的标签
                    if (null == $el) {
                        // TODO: 抛错还是无视？ 这是一个需要思考的问题 ...
                    }
                    // 闭合标签
                    else {
                        this.$current = $el.parentElement();
                    }
                }
            }
            // HTML Comment begin: 33
            // "|(<!--)"
            else if (null != m.group(33)) {
                CheapComment $cmt = doc.createComment();
                $cmt.appendTo($current);
                // 那么就狂野的开始寻找结束标签咯
                pos = input.indexOf("-->", e);
                // 找到了，就结束
                if (pos > 0) {
                    String text = input.substring(e, pos);
                    $cmt.appendText(text);
                    pos += 3;
                    continue;
                }
                // 没有找到，整行作为注释内容，压栈，等下一行
                else {
                    String text = input.substring(e);
                    $cmt.appendText(text);
                    this.$current = $cmt;
                    return;
                }
            }
            //
            // 偏移，准备下一波
            //
            pos = e;
        }

        // 余下的内容
        if (pos < input.length()) {
            String text = input.substring(pos);
            doc.createTextNode(text).appendTo($current);
        }
    }

    private static String R_img_alt = "^(" // Size: 1
                                      // Width: 2, 3(Val), 4(Unit)
                                      + "(([0-9.]+)(rem|px|%)?)?"
                                      + "("
                                      + ":"
                                      // Heigh: 6, 7(Val), 8(Unit)
                                      + "(([0-9.]+)(rem|px|%)?)"
                                      + ")?"
                                      + ")" // ~ Size 1
                                      + "([:\\s]"
                                      // Alt: 10
                                      + "(.*))?$";
    private static Pattern PimgAlt = Regex.getPattern(R_img_alt);

    private void processImgElement(Matcher m) {
        CheapElement $el = doc.createElement("img", "as-md");
        // Image:22,23(T),24(Src),25,26(Alt)
        //
        // Title
        //
        String txt = m.group(23);
        if (!Ws.isEmpty(txt)) {
            $el.attr("title", txt);
        }
        //
        // Src
        //
        String src = m.group(24);
        $el.attr("src", src);
        //
        // Alt
        //
        String alt = m.group(26);
        if (null != alt) {
            // : Customized size
            Matcher m2 = PimgAlt.matcher(alt);
            if (m2.find()) {
                // Width: 2, 3(Val), 4(Unit)
                // "(([0-9.]//)(rem|px|%)?)?"
                String imgW = m2.group(2);
                if (null != imgW) {
                    $el.attr("width", imgW);
                }
                // Heigh: 6, 7(Val), 8(Unit)
                // "(([0-9.]+)(rem|px|%)?)"
                String imgH = m2.group(6);
                if (null != imgH) {
                    $el.attr("height", imgH);
                }
                // Alt: 10
                // "(.*))?$";
                alt = m2.group(10);
            }
            // Alt text
            if (!Ws.isEmpty(alt)) {
                $el.attr("alt", alt);
            }
        }
        $el.appendTo($current);
    }

    private void processLinkReferElement(Matcher m) {
        // Linke Refer:19,20(T),21(Refer)
        //
        // Text
        //
        CheapElement $el = processFontElement(m, "a", 20);
        $el.addClass("is-ref");
        //
        // Href
        //
        String href = Ws.trim(m.group(21));
        if (!href.startsWith("#")) {
            href = "#" + href;
        }
        $el.attr("href", href);
    }

    private void processLinkElement(Matcher m) {
        //
        // Text
        //
        CheapElement $el = processFontElement(m, "a", 15);
        //
        // Href
        //
        String href = m.group(16);
        $el.attr("href", href);
        //
        // Alt
        //
        String alt = m.group(18);
        if (null != alt) {
            // : New window to open
            if (alt.startsWith("+")) {
                $el.attr("target", "_blank");
                alt = alt.substring(1);
            }
            // Alt text
            if (!Ws.isEmpty(alt)) {
                $el.attr("alt", alt);
            }
        }
    }

    public CheapDocument invoke(String input) {
        String[] lines = input.split("\r?\n");
        return invoke(lines);
    }

    public CheapDocument invoke(String[] lines) {
        // 扫描文档体，将行集合成块
        blocks = BP.invoke(lines);

        // 处理文档头部标签
        initDocHead(BP.header);

        // 根据扫描出来的文档块，深入解析文档结构
        for (CheapBlock block : blocks) {
            ParseBlock parser = checkParser(block.type);
            parser.invoke(this, block);
        }

        // 准备好文档
        return doc.ready();
    }

    private void initDocHead(NutMap metas) {
        if (!metas.isEmpty()) {
            // 准备文档头
            CheapElement $head = doc.head();
            if (null == $head) {
                $head = doc.createElement("head");
            }
            doc.root().prepend($head);
            // 依次增加头部内容
            for (Map.Entry<String, Object> en : metas.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                if (null == val) {
                    continue;
                }
                String str;
                CheapElement el;
                // 列表模式
                if (val instanceof Collection<?>) {
                    str = Wcol.join((Collection<?>) val, ",");
                }
                // 纯值
                else {
                    str = val.toString();
                }
                // 针对标题
                if ("title".equals(key)) {
                    el = doc.createElement("title");
                    el.setText(str);

                }
                // 其他的算作 meta
                else {
                    el = doc.createElement("meta");
                    el.attr("name", key);
                    el.attr("content", str);
                }
                el.appendTo($head);
            }
            doc.metas().putAll(metas);
        }
    }

    ParseBlock checkParser(LineType type) {
        ParseBlock pb = parser.get(type);
        if (null == pb) {
            throw Wlang.makeThrow("Invalid block type [%s] : %s", type);
        }
        return pb;
    }

    CheapElement createElement(String tagName, CheapBlock block) {
        return createElement(tagName, block.line(0));
    }

    CheapElement createElement(String tagName, CheapLine line) {
        return createElement(tagName, null, line);
    }

    CheapElement createElement(String tagName, String className, CheapLine line) {
        if (null == className) {
            className = "as-md";
        } else if (!className.contains("as-md")) {
            className = "as-md " + className;
        }
        CheapElement $el = doc.createElement(tagName.toLowerCase(), className);
        if (null != line) {
            $el.attr("md-line", line.lineNumber);
        }
        return $el;
    }
}
