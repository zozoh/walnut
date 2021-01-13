package org.nutz.walnut.cheap.dom;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public boolean isTag(String tagName) {
        return null != tagName && tagName.toUpperCase().equals(this.tagName);
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
