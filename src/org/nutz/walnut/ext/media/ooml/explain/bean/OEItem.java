package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.json.JsonField;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.api.CheapResourceLoader;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.ooml.OomlEntry;
import org.nutz.walnut.ooml.OomlPackage;
import org.nutz.walnut.util.Ws;

public abstract class OEItem {

    protected OENodeType type;

    protected CheapElement refer;

    @JsonField(ignore = true)
    protected OENode parent;

    /**
     * 对应的 ooml 包
     */
    @JsonField(ignore = true)
    protected OomlPackage ooml;

    /**
     * 本次渲染对应的实体，譬如 document.xml。 通过这个实体，根据约定，可以得到 Relationship 列表等。
     */
    @JsonField(ignore = true)
    protected OomlEntry entry;

    /**
     * 如何加载外部资源。
     * 
     */
    @JsonField(ignore = true)
    protected CheapResourceLoader loader;

    public abstract CheapElement renderTo(CheapElement pEl, NutBean vars);

    public void joinTrace(StringBuilder sb, int depth) {
        String prefix = Ws.repeat("|   ", depth);
        sb.append(prefix);
        sb.append("|-- ");
        sb.append(this.toBrief());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinTrace(sb, 0);
        return sb.toString();
    }

    public String toBrief() {
        String name = Ws.camelCase(type.toString());
        name = Ws.upperFirst(name);
        if (this.hasReferElement()) {
            String rs = refer.toBrief();
            return String.format("{ %s } : %s", name, rs);
        }
        return String.format("{ %s }", name);
    }

    public boolean isVirtualNode() {
        return isType(OENodeType.LOOP) || isType(OENodeType.CONDITION) || isType(OENodeType.BRANCH);
    }

    public boolean isType(OENodeType nodeType) {
        return nodeType == this.type;
    }

    public OENodeType getType() {
        return type;
    }

    public boolean hasReferElement() {
        return null != refer;
    }

    public CheapElement getRefer() {
        return refer;
    }

    public void setRefer(CheapElement refer) {
        this.refer = refer;
    }

    public OENode getRoot() {
        if (null == parent) {
            if (this instanceof OENode) {
                return (OENode) this;
            }
            return null;
        }
        return parent.getRoot();
    }

    public boolean hasParent() {
        return null != parent;
    }

    public OENode getParent() {
        return parent;
    }

    public void setParent(OENode parent) {
        this.parent = parent;
    }

    public OomlPackage getOoml() {
        return ooml;
    }

    public void setOoml(OomlPackage ooml) {
        this.ooml = ooml;
    }

    public CheapResourceLoader getLoader() {
        return loader;
    }

    public void setLoader(CheapResourceLoader loader) {
        this.loader = loader;
    }

    public OomlEntry getEntry() {
        return entry;
    }

    public void setEntry(OomlEntry entry) {
        this.entry = entry;
    }

}
