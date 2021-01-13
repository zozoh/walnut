package org.nutz.walnut.cheap.markdown;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.cheap.dom.CheapComment;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;
import org.nutz.walnut.util.Ws;

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
     * 扫描完毕的块
     */
    private LinkedList<CheapBlock> blocks;

    private CheapBlockParsing ing;

    public CheapMarkdownParsing() {
        this(true, 4, "markdown", "body");
    }

    public CheapMarkdownParsing(boolean autoBr) {
        this(autoBr, 4, "markdown", "body");
    }

    public CheapMarkdownParsing(boolean autoBr,
                                int tabWidth,
                                String rootTagName,
                                String bodyTagName) {
        this.blocks = new LinkedList<>();
        this.doc = new CheapDocument(rootTagName, bodyTagName);
        this.$current = this.doc.body();
        this.autoBr = autoBr;
        this.ing = new CheapBlockParsing(tabWidth, bodyTagName);
    }

    private static String R_ele_attr = "(" // Size: 1
                                       // Name:2
                                       + "([a-z0-9-]+)"
                                       + "("
                                       + "=\""
                                       // Value: 4
                                       + "([^\"]*?)"
                                       + "\")?)";
    private static Pattern PeleAttr = Regex.getPattern(R_ele_attr);

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

    private static String REGEX_Font = "(" // Start: 1
                                       + "([*]{2}([^*]+)[*]{2})" // STRONG:2,3
                                       + "|([*]([^*]+)[*])" // EM:4,5
                                       + "|(__([^_]+)__)" // B:6,7
                                       + "|(_([^_]+)_)" // I:8,9
                                       + "|(~~([^~]+)~~)" // Del:10,11
                                       + ")";
    private static Pattern Pfont = Regex.getPattern(REGEX_Font);

    private static String REGEX = "(" // Start: 1
                                  + "([*]{2}([^*]+)[*]{2})" // STRONG:2,3
                                  + "|([*]([^*]+)[*])" // EM:4,5
                                  + "|(__([^_]+)__)" // B:6,7
                                  + "|(_([^_]+)_)" // I:8,9
                                  + "|(~~([^~]+)~~)" // Del:10,11
                                  + "|(`([^`]+)`)" // Code:12,13
                                  // Link:14,15(T),16(Href),17,18(Alt)
                                  + "|(\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
                                  // Image:19,20(T),21(Src),22,23(Alt)
                                  + "|(!\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
                                  // HTML Tag begin: 24,25(Name),26,27(Attrs)
                                  + "|(<([a-z1-6]+)(\\s([^>]+))?>)"
                                  // HTML Tag end: 28,29(name)
                                  + "|(</([a-z1-6]+)>)"
                                  // HTML Comment begin: 30
                                  + "|(<!--)"
                                  + ")";
    private static Pattern P = Regex.getPattern(REGEX);

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
                throw Lang.impossible();
            }
        }
        // 尾部
        if (pos < input.length()) {
            String text = input.substring(pos);
            doc.createTextNode(text).appendTo($current);
        }
    }

    CheapElement createElement(String tagName, CheapBlock block) {
        return createElement(tagName, block.line(0));
    }

    CheapElement createElement(String tagName, CheapLine line) {
        CheapElement $el = doc.createElement(tagName, "as-md");
        if (null != line) {
            $el.attr("md-line", line.lineNumber);
        }
        return $el;
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

    private int __process_font_matcher(Matcher m) {
        // STRONG:2,3
        // "([*]{2}([^*]+)[*]{2})"
        if (null != m.group(2)) {
            processFontElement(m, "strong", 3);
        }
        // EM:4,5
        // "|([*]([^*]+)[*])"
        else if (null != m.group(4)) {
            processFontElement(m, "em", 5);
        }
        // B:6,7
        // "|(__([^_]+)__)"
        else if (null != m.group(6)) {
            processFontElement(m, "b", 7);
        }
        // I:8,9
        // "|(_([^_]+)_)"
        else if (null != m.group(8)) {
            processFontElement(m, "i", 9);
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
        int pos = 0;
        String input = line.content;

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
        while (m.find()) {
            // 位置
            int s = m.start();

            // 前置内容
            if (s > pos) {
                String text = input.substring(pos, s);
                doc.createTextNode(text).appendTo($current);
            }

            // STRONG:2,3
            // "([*]{2}([^*]+)[*]{2})"
            // EM:4,5
            // "|([*]([^*]+)[*])"
            // B:6,7
            // "|(__([^_]+)__)"
            // I:8,9
            // "|(_([^_]+)_)"
            // Del:10,11
            // "|(~~([^~]+)~~)"
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
            // // Link:14,15(T),16(Href),17,18(Alt)
            // "|(\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
            else if (null != m.group(14)) {
                processLinkElement(m);
            }
            // // Image:19,20(T),21(Src),22,23(Alt)
            // "|(!\\[(.*)\\]\\(([^\\s]+)(\\s+\"(.+?)\")?\\))"
            else if (null != m.group(19)) {
                processImgElement(m);
            }
            // // HTML Tag begin: 24,25(Name),26,27(Attrs)
            // "|(<([a-z1-6]+)(\\s([^>]+))?>)"
            else if (null != m.group(24)) {
                String tagName = m.group(25);
                CheapElement $el = doc.createElement(tagName);

                // 解析属性
                String attrs = Ws.trim(m.group(27));
                Matcher m2 = PeleAttr.matcher(attrs);
                while (m2.find()) {
                    String name = m.group(2);
                    String value = m.group(4);
                    $el.attr(name, value);
                }

                // 压栈
                $current = $el;
            }
            // // HTML Tag end 28,29(name)
            // "|(</([a-z1-6]+)>)"
            else if (null != m.group(28)) {
                String tagName = m.group(29);
                CheapElement $el = (CheapElement) $current;
                while (!$el.isTag(tagName) && $el.hasParent()) {
                    $el = $el.parentElement();
                    if ($el.isBodyElement()) {
                        $el = null;
                        break;
                    }
                }
                // 木有找到可以闭合的标签
                if (null == $el) {
                    // TODO: 抛错还是无视？ 这是一个需要思考的问题 ...
                }
                // 闭合标签
                else {
                    this.$current = $el.parentElement();
                }
            }
            // HTML Comment begin: 30
            // "|(<!--)"
            else if (null != m.group(30)) {
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
    }

    private void processImgElement(Matcher m) {
        CheapElement $el = doc.createElement("img", "as-md");
        //
        // Title
        //
        String txt = m.group(20);
        if (!Ws.isEmpty(txt)) {
            $el.attr("title", txt);
        }
        //
        // Src
        //
        String src = m.group(21);
        $el.attr("src", src);
        //
        // Alt
        //
        String alt = m.group(18);
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
        $el.append($current);
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

    public CheapDocument parseDoc(String[] lines) {
        // 扫描文档体，将行集合成块
        blocks = ing.parseBlocks(lines);

        // 根据扫描出来的文档块，深入解析文档结构
        for (CheapBlock block : blocks) {
            ParseBlock pb = parser.get(block.type);
            if (null == pb) {
                throw Lang.makeThrow("Invalid block type [%s] : %s", block.type, block.toString());
            }
            pb.invoke(this, block);
        }

        // 准备好文档
        doc.ready();
        return doc;
    }

}
