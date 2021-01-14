package org.nutz.walnut.cheap.html;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.cheap.dom.CheapComment;
import org.nutz.walnut.cheap.dom.CheapDocument;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.cheap.dom.CheapNode;
import org.nutz.walnut.cheap.dom.CheapRawData;
import org.nutz.walnut.cheap.dom.CheapText;
import org.nutz.walnut.util.Ws;

public class CheapHtmlParsing {

    /**
     * 文档体元素
     */
    CheapDocument doc;

    /**
     * 当前解析元素
     */
    CheapElement $current;

    public CheapHtmlParsing() {
        this.doc = new CheapDocument(null, null);
    }

    private static String REGEX = "(" // Start: 1
                                  // TagBegin: 2,3(Name),4,5(Attrs)
                                  + "(<([A-Za-z0-9_:-]+)(\\s([^>]+))?>)"
                                  // TagEnd: 6,7(name)
                                  + "|(</([A-Za-z0-9_:-]+)>)"
                                  // Comment begin: 8
                                  + "|(<!--)"
                                  // CData begin: 9
                                  + "|(<!\\[CDATA\\[)"
                                  + ")";
    private static Pattern P = Regex.getPattern(REGEX);

    public CheapDocument invoke(String input) {
        int pos = 0;
        Matcher m = P.matcher(input);

        // 遍历查找
        while (m.find(pos)) {
            int s = m.start();
            int e = m.end();

            // 前置
            if (s > pos) {
                String text = input.substring(pos, s);
                CheapText $t = doc.createTextNode(text);
                pushNode($t);
            }

            // 指向下一个位置
            pos = e;

            // TagBegin: 2,3(Name),4,5(Attrs)
            // "|(<([a-z1-6]+)(\\s([^>]+))?>)"
            if (null != m.group(2)) {
                String tagName = m.group(3);
                CheapElement $el = doc.createElement(tagName);
                // 还未初始化
                if (null == $current) {
                    doc.setRootElement($el);
                    $current = $el;
                }
                // 加入 DOM 树
                else {
                    $el.appendTo($current);
                }
                // 解析属性
                String attrs = m.group(5);
                if (null != attrs) {
                    NutMap bean = Ws.splitAttrMap(attrs);
                    $el.attrs(bean);
                }

                // 不能直接结束的标签，需要压栈
                if (!$el.isClosedTag()) {
                    $current = $el;
                }
            }
            // TagEnd: 6,7(name)
            // "|(</([a-z1-6]+)>)"
            else if (null != m.group(6)) {
                if (null != $current) {
                    String tagName = m.group(7).toUpperCase();
                    // 当前就是
                    if ($current.isTagName(tagName)) {
                        $current = (CheapElement) $current.getParent();
                    }
                    // 向上查找
                    else {
                        CheapElement $el = $current.getClosest(tagName);
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
            }
            // Comment begin: 8
            // "|(<!--)"
            else if (null != m.group(8)) {
                CheapComment $cmt = doc.createComment();
                this.pushNode($cmt);
                // 那么就狂野的开始寻找结束标签咯
                pos = input.indexOf("-->", e);
                // 找到了，就结束
                if (pos > 0) {
                    String text = input.substring(e, pos);
                    $cmt.setText(text);
                    pos += 3;
                    continue;
                }
                // 没有找到，全部作为注释
                else {
                    String text = input.substring(e);
                    $cmt.appendText(text);
                    this.$current = null;
                    pos = input.length();
                    continue;
                }
            }
            // CData begin: 9
            // "|(<![CDATA[)"
            else if (null != m.group(9)) {
                CheapRawData $cmt = doc.createRawData();
                this.pushNode($cmt);
                // 那么就狂野的开始寻找结束标签咯
                pos = input.indexOf("]]>", e);
                // 找到了，就结束
                if (pos > 0) {
                    String text = input.substring(e, pos);
                    $cmt.setData(text);
                    pos += 3;
                    continue;
                }
                // 没有找到，全部作为注释
                else {
                    String text = input.substring(e);
                    $cmt.setData(text);
                    this.$current = null;
                    pos = input.length();
                    continue;
                }
            }

        }

        // 最后一部分
        if (pos < input.length() && null != $current) {
            String text = input.substring(pos);
            doc.createTextNode(text).appendTo($current);
        }

        // 准备好文档
        doc.ready();
        return doc;
    }

    private void pushNode(CheapNode node) {
        // 记入当前节点
        if (null != $current) {
            node.appendTo($current);
        }
        // 还未遇到根节点
        else if (!doc.hasRootElement()) {
            doc.addHeadNode(node);
        }
        // 根节点已闭合
        else {
            doc.addTailNodes(node);
        }
    }

}
