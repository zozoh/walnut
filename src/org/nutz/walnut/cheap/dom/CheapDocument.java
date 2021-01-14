package org.nutz.walnut.cheap.dom;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;

public class CheapDocument {

    private NutBean header;

    /**
     * 在根元素之前的元素列表
     */
    private List<CheapNode> headNodes;

    /**
     * 在根元素之后的元素列表
     */
    private List<CheapNode> tailNodes;

    private CheapElement $root;

    private CheapElement $body;

    public CheapDocument() {
        this("html", "body");
    }

    public CheapDocument(String rootTagName, String bodyTagName) {
        header = new NutMap();
        headNodes = new LinkedList<>();
        tailNodes = new LinkedList<>();
        if (null != rootTagName) {
            $root = new CheapElement(rootTagName);
            $root.doc = this;
            if (null != bodyTagName) {
                $body = new CheapElement(bodyTagName);
                $body.appendTo($root);
            }
            $root.rebuildChildrenIndex();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        // 根元素
        if (null != $root) {
            $root.joinTree(sb, 0, "|   ");
        } else {
            sb.append("~~nil root~~");
        }

        return sb.toString();
    }

    public String toMarkup() {
        StringBuilder sb = new StringBuilder();
        // 加入开头
        for (CheapNode node : headNodes) {
            node.joinString(sb);
        }
        // 根元素
        if (null != $root) {
            $root.joinString(sb);
        }
        // 加入结尾
        for (CheapNode node : tailNodes) {
            node.joinString(sb);
        }

        return sb.toString();
    }

    public String toHtml(String tab) {
        // 根据 HTML 语法格式化
        this.formatAsHtml(tab);

        // 输出
        return this.toMarkup();
    }

    public void format(String tab, String newLineTag) {
        if (null != $root) {
            Pattern p = null;
            if (null != newLineTag) {
                p = Regex.getPattern(newLineTag);
            }
            $root.format(tab, -1, p);
        }
    }

    final static String NL_TAGS = "^(DIV"
                                  + "|UL|OL|LI|DT|DD|DL"
                                  + "|H[1-6]|P|BLOCKQUOTE|PRE"
                                  + "|SCRIPT|TEMPLATE"
                                  + "|TABLE|THEAD|TBODY|TFOOT|TR"
                                  + "|ARTICLE|ASIDE|ADDRESS|NAV"
                                  + "|HEADER|SECTION|FOOTER|MAIN"
                                  + "|AUDIO|VIDEO"
                                  + "|HR"
                                  + "|HEAD|BODY|META|LINKE|TITLE)$";

    public void formatAsHtml(String tab) {
        format(tab, NL_TAGS);
    }

    public void removeEmpty() {
        this.headNodes = filterEmptyNodes(headNodes);
        this.tailNodes = filterEmptyNodes(tailNodes);
        if (null != this.$root) {
            this.$root.filterEmptyChildren();
        }
    }

    protected LinkedList<CheapNode> filterEmptyNodes(List<CheapNode> nodes) {
        if (null == nodes)
            return null;
        LinkedList<CheapNode> list = new LinkedList<>();
        for (CheapNode node : nodes) {
            if (!node.isEmpty() || node.isElement()) {
                list.add(node);
            }
        }
        return list;
    }

    public CheapElement createElement(String tagName) {
        return createElement(tagName, null);
    }

    public CheapElement createElement(String tagName, String className) {
        CheapElement $node = new CheapElement(tagName, className);
        $node.doc = this;
        return $node;
    }

    public CheapText createTextNode() {
        return createTextNode(null);
    }

    public CheapText createTextNode(String text) {
        CheapText $node = new CheapText(text);
        $node.doc = this;
        return $node;
    }

    public CheapComment createComment() {
        return createComment(null);
    }

    public CheapComment createComment(String text) {
        CheapComment $node = new CheapComment(text);
        $node.doc = this;
        return $node;
    }

    public CheapRawData createRawData() {
        return createRawData(null);
    }

    public CheapRawData createRawData(String data) {
        CheapRawData $node = new CheapRawData(data);
        $node.doc = this;
        return $node;
    }

    public NutBean getHeader() {
        return header;
    }

    public void setHeader(NutBean headers) {
        this.header = headers;
    }

    public void setRootElement(CheapElement $root) {
        $root.doc = this;
        this.$root = $root;
    }

    public void setBodyElement(CheapElement $body) {
        this.$body = $body.appendTo($root);
    }

    public boolean hasRootElement() {
        return null != this.$root;
    }

    public CheapElement root() {
        return this.$root;
    }

    public boolean hasBodyElement() {
        return null != this.$body;
    }

    public CheapElement body() {
        return this.$body;
    }

    public boolean hasHeadNodes() {
        return null != headNodes && !headNodes.isEmpty();
    }

    public List<CheapNode> getHeadNodes() {
        return headNodes;
    }

    public void addHeadNode(CheapNode node) {
        headNodes.add(node);
    }

    public void clearHeadNode() {
        headNodes.clear();
    }

    public boolean hasTailNodes() {
        return null != tailNodes && !tailNodes.isEmpty();
    }

    public List<CheapNode> getTailNodes() {
        return tailNodes;
    }

    public void addTailNodes(CheapNode node) {
        tailNodes.add(node);
    }

    public void clearTailNodes() {
        tailNodes.clear();
    }

    public void ready() {
        this.$root.rebuildChildrenIndex();
        // 看看有木有 body
        if (null == this.$body) {
            this.$body = this.$root.getFirstChildElement("body");
        }
    }

}
