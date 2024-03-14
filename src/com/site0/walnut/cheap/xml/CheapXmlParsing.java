package com.site0.walnut.cheap.xml;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import com.site0.walnut.cheap.dom.CheapComment;
import com.site0.walnut.cheap.dom.CheapDocType;
import com.site0.walnut.cheap.dom.CheapDocument;
import com.site0.walnut.cheap.dom.CheapElement;
import com.site0.walnut.cheap.dom.CheapNode;
import com.site0.walnut.cheap.dom.CheapNodeType;
import com.site0.walnut.cheap.dom.CheapRawData;
import com.site0.walnut.cheap.dom.CheapText;
import com.site0.walnut.util.Ws;

public class CheapXmlParsing {

    List<CheapNode> rootNodes;

    /**
     * 文档体元素
     */
    CheapDocument doc;

    /**
     * 当前解析元素
     */
    CheapElement $current;

    public CheapXmlParsing() {
        this(new CheapDocument());
    }

    public CheapXmlParsing(String rootTagName) {
        this(new CheapDocument(rootTagName));
    }

    public CheapXmlParsing(CheapDocument doc) {
        this.doc = doc;
    }

    public void setAutoClosedTags(String autoClosedTags) {
        doc.setAutoClosedTags(autoClosedTags);
    }

    public void setAutoClosedTagsAsHtml() {
        doc.setAutoClosedTagsAsHtml();
    }

    private static String REGEX = "(" // Start: 1
                                  // TagBegin: 2,3(Name),4,5(Attrs),6(Closed)
                                  + "(<([A-Za-z0-9_:-]+)(\\s([^>]+?))?(/)?>)"
                                  // TagEnd: 7,8(name)
                                  + "|(</([A-Za-z0-9_:-]+)>)"
                                  // Comment begin: 9
                                  + "|(<!--)"
                                  // CData begin: 10
                                  + "|(<!\\[CDATA\\[)"
                                  + ")";
    private static Pattern P = Regex.getPattern(REGEX);

    public CheapDocument parseDoc(String input) {
        // 首先 解析文档
        this.parseFragment(input);

        String rootTagName = doc.getStdRootTagName();

        // 解析出了根节点
        CheapElement taEl = findInRootNodes(rootTagName);
        if (null != taEl) {
            setRootNodesToDocByRootTagName(rootTagName);
            doc.ready();
            return doc;
        }
        // Doc 没有根节点，那么从解析的结果中找到第一个元素作为根节点
        if (!doc.hasRootElement()) {
            setRootNodesToDocByRootTagName(null);
            doc.ready();
            return doc;
        }

        //
        // 下面就是，doc 有根节点，但是没在 rootNodes 里找到
        //

        // 那么解析结果中有木有 head节点呢？
        String headTagName = doc.getStdHeadTagName();
        CheapElement headEl = findInRootNodes(headTagName);
        if (null != headEl) {
            doc.setHeadElement(headEl);
        }

        // 结果中，有没有 body呢
        String bodyTagName = doc.getStdBodyTagName();
        CheapElement bodyEl = findInRootNodes(bodyTagName);
        if (null != bodyEl) {
            doc.setBodyElement(bodyEl);
        }

        // 准备三段列表
        List<CheapNode> prevs = new LinkedList<>();
        List<CheapNode> middles = new LinkedList<>();
        List<CheapNode> tails = new LinkedList<>();
        Iterator<CheapNode> it = rootNodes.iterator();

        // 遇到了 head 表示 prevs 截止
        CheapNode node;
        while (it.hasNext()) {
            node = it.next();
            // 文档声明，则记录一下
            if (node.isType(CheapNodeType.DOC_TYPE)) {
                doc.setDocType((CheapDocType) node);
                continue;
            }
            // 遇到头，表示截止
            if (it == headEl) {
                break;
            }
            prevs.add(node);
        }
        // 遇到了 body 表示 middles 截止
        while (it.hasNext()) {
            node = it.next();
            if (it == bodyEl) {
                break;
            }
            middles.add(node);
        }
        // 后面的收集到 tails 里
        while (it.hasNext()) {
            tails.add(it.next());
        }

        // 文档有 head/body
        if (doc.hasHeadElement() && doc.hasBodyElement()) {
            if (headEl != null && bodyEl != null) {
                headEl.insertPrevNodes(prevs);
                headEl.insertNextNodes(middles);
                bodyEl.insertNextNodes(tails);
            } else {
                doc.body().setChildren(rootNodes);
            }
        }
        // 文档只有 head 元素
        else if (doc.hasHeadElement()) {
            if (null != headEl) {
                headEl.insertPrevNodes(prevs);
                headEl.insertNextNodes(middles);
                doc.root().appendChildren(tails);
            } else {
                doc.root().setChildren(rootNodes);
            }
        }
        // 文档只有 body 元素
        else if (doc.hasBodyElement()) {
            if (null != bodyEl) {
                bodyEl.insertPrevNodes(prevs);
                bodyEl.insertPrevNodes(middles);
                bodyEl.insertNextNodes(tails);
            } else {
                doc.body().setChildren(rootNodes);
            }
        }
        // 文档只有根节点
        else {
            doc.root().setChildren(rootNodes);
        }

        // 准备好文档
        doc.ready();
        return doc;
    }

    private void setRootNodesToDocByRootTagName(String rootTagName) {
        Iterator<CheapNode> it = rootNodes.iterator();
        // 一直找到 root
        while (it.hasNext()) {
            CheapNode rootNode = it.next();
            // 文档声明，则记录一下
            if (rootNode.isType(CheapNodeType.DOC_TYPE)) {
                doc.setDocType((CheapDocType) rootNode);
                continue;
            }
            // 看看是否可以作为根节点
            if (rootNode.isElement()) {
                CheapElement el = (CheapElement) rootNode;
                if (null == rootTagName || el.isStdTagName(rootTagName)) {
                    doc.setRootElement(el);
                    break;
                }
            }
            // 其余作为节点前缀
            doc.addPrevNode(rootNode);
        }
        // 记入结尾节点
        while (it.hasNext()) {
            CheapNode rootNode = it.next();
            doc.addTailNodes(rootNode);
        }
    }

    private CheapElement findInRootNodes(String tagName) {
        CheapElement targetEl = null;
        if (null != tagName) {
            for (CheapNode rootNode : rootNodes) {
                if (rootNode.isElement()) {
                    CheapElement el = (CheapElement) rootNode;
                    if (el.isStdTagName(tagName)) {
                        targetEl = el;
                        break;
                    }
                }
            }
        }
        return targetEl;
    }

    private void pushNode(CheapNode node) {
        // 记入当前节点
        if (null != $current) {
            node.appendTo($current);
        }
        // 否则记入顶级节点
        else {
            rootNodes.add(node);
        }
        // 是否切换当前节点呢？
        if (node.isElement()) {
            CheapElement el = (CheapElement) node;
            if (!el.isClosedTag()) {
                this.$current = el;
            }
        }
    }

    public List<CheapNode> parseFragment(String input) {
        // 初始化内部变量
        this.rootNodes = new LinkedList<>();
        this.$current = null;
        int pos = 0;
        Matcher m = P.matcher(input);

        // 遍历查找
        while (m.find(pos)) {
            int s = m.start();
            int e = m.end();

            // 前置
            if (s > pos) {
                String text = input.substring(pos, s);
                String trimLower = Ws.trim(text).toLowerCase();
                // HTML 文档声明
                if (trimLower.matches("^<!doctype\\s+html>")) {
                    CheapDocType dt = new CheapDocType();
                    dt.setHtml(true);
                    this.pushNode(dt);
                }
                // XML 文档声明
                else if (trimLower.startsWith("<?xml") && trimLower.endsWith("?>")) {
                    CheapDocType dt = new CheapDocType();
                    dt.setHtml(false);
                    int end = trimLower.length() - 2;
                    String attrs = text.substring(5, end);
                    NutMap attMap = Ws.splitAttrMap(attrs);
                    dt.propsPutAll(attMap);
                    this.pushNode(dt);
                }
                // 作为普通文本节点
                else {
                    CheapText $t = doc.createTextNode(text);
                    this.pushNode($t);
                }
            }

            // 指向下一个位置
            pos = e;

            // TagBegin: 2,3(Name),4,5(Attrs)
            // "|(<([a-z1-6]+)(\\s([^>]+))?>)"
            if (null != m.group(2)) {
                String tagName = m.group(3);
                CheapElement $el = doc.createElement(tagName);

                // 解析数据列表
                String attrs = m.group(5);
                if (null != attrs) {
                    NutMap bean = Ws.splitAttrMap(attrs);
                    // 是否为关闭标签
                    if (bean.containsKey("/")) {
                        $el.setClosed(true);
                        bean.remove("/");
                    }
                    $el.setAttrs(bean);
                }

                // 是否为自动关闭标签
                $el.setClosed("/".equals(m.group(6)));

                // 加入解析结果
                this.pushNode($el);
            }
            // TagEnd: 7,8(name)
            // "|(</([a-z1-6]+)>)"
            else if (null != m.group(7)) {
                if (null != $current) {
                    String tagName = m.group(8);
                    // 当前就是
                    if ($current.isTag(tagName)) {
                        $current = (CheapElement) $current.getParent();
                    }
                    // 向上查找
                    else {
                        CheapElement $el = $current.getClosestByTagName(tagName);
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
            // Comment begin: 9
            // "|(<!--)"
            else if (null != m.group(9)) {
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
            // CData begin: 10
            // "|(<![CDATA[)"
            else if (null != m.group(10)) {
                CheapRawData $cmt = doc.createRawData();
                this.pushNode($cmt);
                // 那么就狂野的开始寻找结束标签咯
                pos = input.indexOf("]]>", e);
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
                    $cmt.setText(text);
                    this.$current = null;
                    pos = input.length();
                    continue;
                }
            }

        }

        // 最后一部分
        if (pos < input.length() && null != $current) {
            String text = input.substring(pos);
            CheapNode $t = doc.createTextNode(text);
            this.pushNode($t);
        }

        // 返回解析的节点列表
        return rootNodes;
    }
}
