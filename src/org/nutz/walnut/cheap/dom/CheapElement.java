package org.nutz.walnut.cheap.dom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.Wcol;
import org.nutz.walnut.util.Ws;

public class CheapElement extends CheapNode {

    private String tagName;

    private List<String> className;

    private NutBean attrs;

    protected CheapElement(String tagName) {
        this(tagName, null);
    }

    protected CheapElement(String tagName, String className) {
        this.type = CheapNodeType.ELEMENT;
        this.setTagName(tagName);
        this.setClassName(className);
        this.attrs = new NutMap();
    }

    @Override
    public void joinTree(StringBuilder sb, int depth, String tab) {
        sb.append(Ws.repeat(tab, depth));
        String prefix = "";
        if (depth > 0) {
            prefix = "|-- ";
        }
        // 下标
        sb.append(String.format("%s[%d]%s(%d): ",
                                prefix,
                                this.getNodeIndex(),
                                tagName,
                                this.getElementIndex()));
        // 标签
        if (null != className) {
            sb.append('.');
            sb.append(Wcol.join(className, " "));
        }
        // 输出属性
        for (String key : attrs.keySet()) {
            sb.append(" @").append(key);
        }
        // 换行
        sb.append("\n");

        // 输出子节点
        if (this.hasChildren()) {
            for (CheapNode child : children) {
                child.joinTree(sb, depth + 1, tab);
            }
        }
    }

    @Override
    public void format(String tab, int depth, Pattern newLineTag) {
        // 不是块元素就无视
        if (!this.isRoot() && (null == newLineTag || newLineTag.matcher(tagName).find())) {
            // 在前面插入一个文本节点作为缩进
            if (depth >= 0) {
                String prefix = "\n" + Ws.repeat(tab, depth);
                // ------------------------------------------------
                // 前面的文本节点是否满足这个格式呢？
                if (null != prev && prev.isText()) {
                    if (!prev.isProp("cheap-format", true)) {
                        CheapText $text = (CheapText) prev;
                        // 没有的话搞一个过去
                        if (!$text.isTextEndsWith(prefix)) {
                            $text.appendText(prefix);
                        }
                        $text.prop("cheap-format", true);
                    }
                }
                // 前面没有文本节点，那么就搞一个
                else {
                    CheapText $text = this.doc.createTextNode(prefix);
                    this.insertPrev($text.prop("cheap-format", true));
                }
                // ------------------------------------------------
                // 后面的文本有换行吗？
                if (null != next && next.isText()) {
                    if (!next.isProp("cheap-format", true)) {
                        CheapText $text = (CheapText) next;
                        // 没有的话搞一个过去
                        if ($text.isTextStartsWith(prefix)) {
                            $text.prependText(prefix);
                        }
                        $text.prop("cheap-format", true);
                    }
                }
                // 后面节点，但不是文本，搞一个同等缩进的过去
                else if (null != next) {
                    if (!next.isProp("cheap-foramt", true)) {
                        CheapText $text = this.doc.createTextNode(prefix);
                        this.insertNext($text.prop("cheap-format", true));
                    }
                }
                // 后面根本没有节点，那么就搞一个回退一级的文本节点过去
                else {
                    String prefix9 = prefix;
                    if (depth > 0) {
                        prefix9 = prefix.substring(0, prefix.length() - tab.length());
                    }
                    CheapText $text = this.doc.createTextNode(prefix9);
                    this.insertNext($text.prop("cheap-format", true));
                }
                // ------------------------------------------------
                // 内部超过一个节点，那么需要内部换行
                if (this.hasChildren() && this.children.size() > 1) {
                    // 前面插一个
                    CheapNode $first = this.children.getFirst();
                    if (!$first.isProp("cheap-format", true)) {
                        String prefix2 = prefix + tab;
                        // 合并文本节点
                        if ($first.isText()) {
                            CheapText $ft = (CheapText) $first;
                            if (!$ft.isTextStartsWith(prefix2)) {
                                $ft.prependText(prefix2);
                            }
                            $ft.prop("cheap-format", true);
                        }
                        // 新搞一个
                        else {
                            CheapText $text = this.doc.createTextNode(prefix2);
                            $first.insertPrev($text.prop("cheap-format", true));
                        }
                    }

                    // 后面插一个
                    CheapNode $last = this.children.getLast();
                    if (!$last.isProp("cheap-format", true)) {
                        // 合并文本节点
                        if ($last.isText()) {
                            CheapText $lt = (CheapText) $last;
                            if (!$lt.isTextEndsWith(prefix)) {
                                $lt.appendText(prefix);
                            }
                            $lt.prop("cheap-format", true);
                        }
                        // 新搞一个
                        else {
                            CheapText $text = this.doc.createTextNode(prefix);
                            $last.insertNext($text.prop("cheap-format", true));
                        }
                    }
                }
                // ------------------------------------------------
            }
        }

        // 处理子节点
        if (this.hasChildren()) {
            CheapNode[] ary = new CheapNode[children.size()];
            children.toArray(ary);
            for (CheapNode child : ary) {
                child.format(tab, depth + 1, newLineTag);
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    public void joinString(StringBuilder sb) {
        // 输出的标签名强制小写
        String displayTagName = tagName.toLowerCase();

        // 标签开始
        sb.append('<').append(displayTagName);

        // 输出 className
        if (null != this.className) {
            sb.append(" class=\"");
            sb.append(Wcol.join(className, " "));
            sb.append('"');
        }

        // 输出属性
        for (Map.Entry<String, Object> en : attrs.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();
            // 裸名
            if (null == val) {
                sb.append(' ').append(key);
            }
            // 完整名
            else {
                sb.append(' ').append(key);
                sb.append("=\"").append(val).append('"');
            }
        }
        sb.append('>');

        // 输出子节点
        if (this.hasChildren()) {
            for (CheapNode child : this.children) {
                child.joinString(sb);
            }
            // 标签结束
            sb.append("</").append(displayTagName).append('>');
        }
        // 输出结束标签
        else if (!this.isClosedTag()) {
            sb.append("</").append(displayTagName).append('>');
        }
    }

    public CheapElement parentElement() {
        return (CheapElement) this.getParent();
    }

    public List<CheapElement> childrenElements() {
        if (!this.hasChildren()) {
            return new LinkedList<>();
        }

        List<CheapNode> nodes = this.getChildren();
        List<CheapElement> list = new ArrayList<>(nodes.size());
        for (CheapNode node : nodes) {
            if (node.isElement()) {
                list.add((CheapElement) node);
            }
        }
        return list;
    }

    public CheapElement getFirstChildElement(String tagName) {
        if (null != tagName) {
            tagName = tagName.toUpperCase();
        }
        for (CheapNode node : children) {
            if (node.isElement()) {
                CheapElement $el = (CheapElement) node;
                if (null == tagName) {
                    return $el;
                }
                if ($el.isTagName(tagName)) {
                    return $el;
                }
            }
        }
        return null;
    }

    /**
     * @return 当前标签是否是自结束标签
     */
    public boolean isClosedTag() {
        return isTagAs("^(IMG|BR|HR)$");
    }

    /**
     * @param regex
     *            标签名的正则表达式。标签名全部用大写形式匹配
     * @return 是否符合标签
     */
    public boolean isTagAs(String regex) {
        String name = tagName.toUpperCase();
        return name.matches(regex);
    }

    /**
     * @param tagName
     *            标签名
     * @return 是否符合标签（大小写不敏感）
     */
    public boolean isTag(String tagName) {
        return null != tagName && tagName.toUpperCase().equals(this.tagName);
    }

    /**
     * @param tagName
     *            标签名
     * @return 是否符合标签（大小写敏感）
     */
    public boolean isTagName(String tagName) {
        return null != tagName && tagName.equals(this.tagName);
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName.trim().toUpperCase();
    }

    public boolean hasClassName() {
        return null != className && !className.isEmpty();
    }

    public String getClassName() {
        if (null == className)
            return null;
        return Wcol.join(className, " ");
    }

    public void setClassName(String className) {
        if (null == className) {
            this.className = null;
        } else {
            this.className = Ws.splitIgnoreBlanks(className, "\\s+");
        }
    }

    public CheapElement addClass(String className) {
        if (null == this.className) {
            if (null == className)
                return this;
            this.className = new LinkedList<>();
        }
        this.className.addAll(Ws.splitIgnoreBlanks(className, "\\s+"));
        return this;
    }

    public CheapElement uniqClass() {
        if (this.hasClassName()) {
            List<String> list = new LinkedList<>();
            Wcol.uniq(this.className, list);
            this.className = list;
        }
        return this;
    }

    public CheapElement addClassUniq(String className) {
        this.addClass(className);
        return this.uniqClass();
    }

    public CheapElement attrClear() {
        attrs.clear();
        return this;
    }

    public Set<String> attrNames() {
        return attrs.keySet();
    }

    public Set<Map.Entry<String, Object>> attrEntrySet() {
        return attrs.entrySet();
    }

    public boolean hasAttr(String name) {
        return attrs.has(name);
    }

    public boolean isAttr(String name, Object val) {
        return attrs.is(name, val);
    }

    public CheapElement attr(String name, Object val) {
        attrs.put(name, val);
        return this;
    }

    public CheapElement attrs(Map<String, Object> bean) {
        if (null != bean) {
            attrs.putAll(bean);
        }
        return this;
    }

    public String attr(String name) {
        return attrs.getString(name);
    }

    public String attrString(String name, String dft) {
        return attrs.getString(name, dft);
    }

    public int attrInt(String name) {
        return attrs.getInt(name);
    }

    public int attrInt(String name, int dft) {
        return attrs.getInt(name, dft);
    }

    public long attrLong(String name) {
        return attrs.getLong(name);
    }

    public long attrLong(String name, long dft) {
        return attrs.getLong(name, dft);
    }

    public boolean attrBoolean(String name) {
        return attrs.getBoolean(name);
    }

    public boolean attrBoolean(String name, boolean dft) {
        return attrs.getBoolean(name, dft);
    }

    public double attrDouble(String name) {
        return attrs.getDouble(name);
    }

    public double attrDouble(String name, double dft) {
        return attrs.getDouble(name, dft);
    }

    public float attrFloat(String name) {
        return attrs.getFloat(name);
    }

    public float attrFloat(String name, float dft) {
        return attrs.getFloat(name, dft);
    }

    public <T extends Enum<T>> T attrEnum(String name, Class<T> classOfEnum) {
        return attrs.getEnum(name, classOfEnum);
    }

    public boolean attrIsEnum(String name, Enum<?>... eus) {
        return attrs.isEnum(name, eus);
    }

    public CheapElement appendText(String text) {
        // 最后一个节点就是文本节点，那么就融合
        if (this.hasChildren()) {
            CheapNode lastNode = this.getLastChild();
            if (lastNode.isText()) {
                ((CheapText) lastNode).appendText(text);
                return this;
            }
        }
        // 增加一个文本节点
        this.append(new CheapText(text));
        return this;
    }

    @Override
    public CheapElement empty() {
        return (CheapElement) super.empty();
    }

    @Override
    public CheapElement appendTo(CheapNode pnode) {
        return (CheapElement) super.appendTo(pnode);
    }

    @Override
    public CheapElement prependTo(CheapNode pnode) {
        return (CheapElement) super.prependTo(pnode);
    }

    @Override
    public CheapElement append(CheapNode... nodes) {
        return (CheapElement) super.append(nodes);
    }

    @Override
    public CheapElement prepend(CheapNode... nodes) {
        return (CheapElement) super.prepend(nodes);
    }

}
