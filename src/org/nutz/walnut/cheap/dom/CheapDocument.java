package org.nutz.walnut.cheap.dom;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.cheap.dom.mutation.CheapDomOperation;
import org.nutz.walnut.cheap.dom.selector.CheapDomSelector;
import org.nutz.walnut.util.Ws;

public class CheapDocument {

    private NutBean metas;

    /**
     * 在根元素之前的元素列表
     */
    private List<CheapNode> prevNodes;

    /**
     * 在根元素之后的元素列表
     */
    private List<CheapNode> tailNodes;

    private String rootTagName;
    private String headTagName;
    private String bodyTagName;

    private CheapElement $root;

    private CheapElement $head;

    private CheapElement $body;

    /**
     * 不需要结束标记的元素
     */
    // private String brTags;
    private Pattern P_AUTO_CLOSED_TAGS;

    public CheapDocument() {
        this("html", "head", "body");
        this.setAutoClosedTagsAsHtml();
    }

    public CheapDocument(String rootTagName) {
        this(rootTagName, null, null);
    }

    public CheapDocument(String rootTagName, String headTagName, String bodyTagName) {
        this.rootTagName = rootTagName;
        this.headTagName = headTagName;
        this.bodyTagName = bodyTagName;
        prevNodes = new LinkedList<>();
        tailNodes = new LinkedList<>();
        if (null != rootTagName) {
            $root = new CheapElement(rootTagName);
            $root.doc = this;
            if (null != headTagName) {
                $head = new CheapElement(headTagName);
                $head.appendTo($root);
            }
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
        for (CheapNode node : prevNodes) {
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

    public String toHtml() {
        // 根据 HTML 语法格式化
        this.formatAsHtml();

        // 输出
        return this.toMarkup();
    }

    public void format(CheapFormatter fmt) {
        if (null != $root) {
            $root.format(fmt, -1);
        }
    }

    public void compact() {
        $root.compact();
    }

    public void compactWith(CheapNodeFilter flt) {
        $root.compactWith(flt);
    }

    public void compactWithEl(CheapFilter flt) {
        $root.compactWithEl(flt);
    }

    final static CheapFormatter CDF_XML = new CheapFormatter("^.+$", "^.+$");
    final static CheapFormatter CDF_HTML = new CheapFormatter(true);

    public void formatAsXml() {
        format(CDF_XML);
    }

    public void formatAsHtml() {
        format(CDF_HTML);
    }

    public boolean isAutoClosedTag(CheapElement $el) {
        return null != P_AUTO_CLOSED_TAGS
               && P_AUTO_CLOSED_TAGS.matcher($el.uppercaseTagName).find();
    }

    public boolean isHtmlBlockTag(CheapElement $el) {
        return CDF_HTML.isBlock($el);
    }

    public void setAutoClosedTags(String autoClosedTags) {
        // this.brTags = brTags;
        if (null == autoClosedTags) {
            P_AUTO_CLOSED_TAGS = null;
        } else {
            P_AUTO_CLOSED_TAGS = Regex.getPattern(autoClosedTags);
        }
    }

    public void setAutoClosedTagsAsHtml() {
        this.setAutoClosedTags("^(IMG|BR|HR|META|LINK|COL|INPUT)$");
    }

    public void removeEmpty() {
        this.prevNodes = filterEmptyNodes(prevNodes);
        this.tailNodes = filterEmptyNodes(tailNodes);
        if (null != this.$root) {
            this.$root.filterEmptyChildren();
        }
    }

    public void removeBlankNodes() {
        List<CheapNode> list = this.findNodes(e -> e.isText() && ((CheapText) e).isBlank());
        for (CheapNode nd : list) {
            nd.remove();
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

    public CheapElement select(String selector) {
        CheapDomSelector sel = new CheapDomSelector(selector);
        return this.select(sel);
    }

    public CheapElement select(CheapDomSelector selector) {
        if (null == $root) {
            return null;
        }
        return $root.select(selector);
    }

    public List<CheapElement> selectAll(String selector) {
        CheapDomSelector sel = new CheapDomSelector(selector);
        return this.selectAll(sel);
    }

    public List<CheapElement> selectAll(CheapDomSelector selector) {
        if (null == $root) {
            return new LinkedList<>();
        }
        return $root.selectAll(selector);
    }

    public CheapElement findElement(CheapFilter filter) {
        if (null != $root) {
            return $root.findElement(filter);
        }
        return null;
    }

    public List<CheapElement> findElements(CheapFilter filter) {
        if (null != $root) {
            return $root.findElements(filter);
        }
        return new LinkedList<>();
    }

    public void walkElements(CheapFilter filter) {
        if (null != $root) {
            $root.walkElements(filter);
        }
    }

    public CheapNode findNode(CheapNodeFilter filter) {
        if (null != $root) {
            return $root.findNode(filter);
        }
        return null;
    }

    public List<CheapNode> findNodes(CheapNodeFilter filter) {
        if (null != $root) {
            return $root.findNodes(filter);
        }
        return new LinkedList<>();
    }

    public CheapElement createElement(String tagName) {
        return createElement(tagName, null);
    }

    public CheapElement createElement(String tagName, String className) {
        CheapElement $node = new CheapElement(tagName, className);
        $node.doc = this;
        $node.setAutoClosed(this.isAutoClosedTag($node));
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

    /**
     * 创建一个占位文本节点。
     * <p>
     * 所谓<b>占位文本节点</b>就是带有属性(prop)为<code>CheapPlacehold=true</code> 属性的文本节点。在
     * DOM 树被格式化时，比较用将这种文本节点进行缩进对齐。
     * 
     * @param text
     *            文本内容
     * @return 修饰文本节点
     */
    public CheapText createPlaceholdText(String text) {
        CheapText $node = new CheapText(text);
        $node.doc = this;
        $node.prop("CheapPlacehold", true);
        return $node;
    }

    /**
     * 创建一个格式化用文本节点。
     * <p>
     * 所谓<b>格式化用文本节点</b>就是带有属性(prop)为<code>CheapFormated=true</code> 属性的文本节点。在
     * DOM 树被格式化时，会插入这种文本节点。或者将占位文本节点标记为<code>CheapFormated=true</code>
     * <p>
     * 这样反复执行格式化函数，比较容易做到<b>幂等</b>
     * 
     * @param text
     *            文本内容
     * @return 修饰文本节点
     */
    public CheapText createFormatText(String text) {
        CheapText $node = new CheapText(text);
        $node.doc = this;
        $node.prop("CheapFormated", true);
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

    public NutBean metas() {
        if (null == metas) {
            this.metas = new NutMap();
        }
        return metas;
    }

    public void setMetas(NutBean metas) {
        this.metas = metas;
    }

    public void change(CheapDomOperation... opts) {
        if (null != opts) {
            for (CheapDomOperation opt : opts) {
                opt.operate(this.$root);
            }
        }
    }

    public void setRootElement(CheapElement $root) {
        $root.doc = this;
        this.$root = $root;
    }

    public void setHeadElement(CheapElement $head) {
        this.$head = $head.appendTo($root);
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

    public boolean hasHeadElement() {
        return null != this.$head;
    }

    public CheapElement head() {
        return this.$head;
    }

    public boolean hasBodyElement() {
        return null != this.$body;
    }

    public CheapElement body() {
        return this.$body;
    }

    public boolean hasHeadNodes() {
        return null != prevNodes && !prevNodes.isEmpty();
    }

    public List<CheapNode> getPrevNodes() {
        return prevNodes;
    }

    public void addPrevNode(CheapNode node) {
        prevNodes.add(node);
    }

    public void clearHeadNode() {
        prevNodes.clear();
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

    public String getStdRootTagName() {
        return Ws.toUpper(rootTagName);
    }

    public String getStdHeadTagName() {
        return Ws.toUpper(headTagName);
    }

    public String getStdBodyTagName() {
        return Ws.toUpper(bodyTagName);
    }

    public String getRootTagName() {
        return rootTagName;
    }

    public String getHeadTagName() {
        return headTagName;
    }

    public String getBodyTagName() {
        return bodyTagName;
    }

    public CheapDocument ready() {
        if (null != this.$root) {
            this.$root.rebuildChildrenIndex();
            // 看看有木有 head
            if (null == this.$head) {
                this.$head = this.$root.getFirstChildElement(this.headTagName);
            }
            // 看看有木有 body
            if (null == this.$body) {
                this.$body = this.$root.getFirstChildElement(this.bodyTagName);
            }
        }
        return this;
    }

}
