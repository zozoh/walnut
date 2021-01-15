package org.nutz.walnut.cheap.dom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Regex;
import org.nutz.walnut.util.Wcol;
import org.nutz.walnut.util.Ws;

public class CheapElement extends CheapNode {

    String tagName;

    List<String> className;

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
            Object val = attrs.get(key);
            sb.append(" @").append(key);
            if (null != val) {
                sb.append('=').append(val);
            }
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

    private static final Pattern P_BEG_NL = Regex.getPattern("^\\s*\r?\n");
    private static final Pattern P_END_NL = Regex.getPattern("\r?\n\\s*$");

    @Override
    public void format(CheapFormatter cdf, int depth) {
        // 不是块元素就无视
        if (depth >= 0 && !this.isRoot() && null != cdf) {
            String prefix = cdf.getPrefix(depth);
            //
            // 块元素
            //
            if (cdf.isBlock(this)) {
                // #外-前
                __fmt_block_outside_prev(prefix);

                // #外-后
                __fmt_block_outside_next(cdf, prefix);

                // 内部换行
                if (this.hasChildren() && // 有元素节点或者超过一个子节点
                    (this.hasElements() || this.countChildren() > 1)) {
                    // #内-前： 前面插一个
                    __fmt_block_inside_head(cdf, prefix);

                    // #内-后： 前面插一个
                    __fmt_block_inside_tail(prefix);
                }
            }
            //
            // 断行元素
            //
            else if (cdf.isBreakLine(this)) {
                // #外-后
                __fmt_block_outside_next(cdf, prefix);
            }
        }

        // 处理子节点
        if (this.hasChildren()) {
            CheapNode[] ary = new CheapNode[children.size()];
            children.toArray(ary);
            for (CheapNode child : ary) {
                child.format(cdf, depth + 1);
            }
        }
    }

    private void __fmt_block_inside_tail(String prefix) {
        CheapNode $last = this.children.getLast();
        if (!$last.isText() // 不是文本，或者不属于下面的两种情况
            || !($last.isFormated() // 文本未格式化
                 || // 或者以新行开头
                 (((CheapText) $last).isTextMatch(P_BEG_NL)))) {
            CheapText $text = this.doc.createFormatText(prefix);
            $last.insertNext($text);
        }
    }

    private void __fmt_block_inside_head(CheapFormatter cdf, String prefix) {
        CheapNode $first = this.children.getFirst();
        if (!$first.isText() // 不是文本，或者不属于下面的两种情况
            || !($first.isFormated() // 文本未格式化
                 || // 或者以新行结尾
                 ((CheapText) $first).isTextMatch(P_END_NL))) {
            String prefix2 = cdf.shiftTab(prefix);
            CheapText $text = this.doc.createFormatText(prefix2);
            $first.insertPrev($text);
        }
    }

    private void __fmt_block_outside_next(CheapFormatter cdf, String prefix) {
        // 后面根本没有节点: 搞一个回退一级的文本节点过去
        if (null == next) {
            String prefix9 = cdf.unshiftTab(prefix);
            CheapText $text = this.doc.createFormatText(prefix9);
            this.insertNext($text);
        }
        // 看看后面节点: ，但不是文本，或者文本节点未以前缀开头
        // 搞一个同等缩进的过去
        else if (!next.isText() // 不是文本
                 || (!next.isFormated() // 未被格式化过
                     && // 并且文本不是以新行开头
                     !((CheapText) next).isTextMatch(P_BEG_NL))) {
            CheapText $text = this.doc.createFormatText(prefix);
            this.insertNext($text);
        }
    }

    private void __fmt_block_outside_prev(String prefix) {
        if (null == prev // 前面没有文本节点
            || (!prev.isFormated() // 前面未被格式化过
                && // 并且
                (!prev.isText() // 前面不是文本
                 || // 或者文本不是以新行结束
                 !((CheapText) prev).isTextMatch(P_END_NL)))) {
            CheapText $text = this.doc.createFormatText(prefix);
            this.insertPrev($text);
        }
    }

    @Override
    public boolean isEmpty() {
        return !this.hasChildren();
    }

    @Override
    public boolean isBlank() {
        if (this.isEmpty())
            return true;

        for (CheapNode child : children) {
            if (!child.isBlank()) {
                return false;
            }
        }

        return true;
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
        else if (null == doc || !doc.isAutoClosedTag(this)) {
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
        String upperName = tagName;
        return (CheapElement) this.getFirstChild(node -> {
            if (!node.isElement())
                return false;
            if (null == upperName)
                return true;
            return ((CheapElement) node).isTagName(upperName);
        });
    }
    
    public CheapElement getLastChildElement(String tagName) {
        if (null != tagName) {
            tagName = tagName.toUpperCase();
        }
        String upperName = tagName;
        return (CheapElement) this.getLastChild(node -> {
            if (!node.isElement())
                return false;
            if (null == upperName)
                return true;
            return ((CheapElement) node).isTagName(upperName);
        });
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
    public String getText() {
        StringBuilder sb = new StringBuilder();
        if (this.hasChildren()) {
            for (CheapNode child : children) {
                sb.append(child.getText());
            }
        }
        return sb.toString();
    }

    @Override
    public void setText(String text) {
        this.empty();
        this.append(new CheapText(text));
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
