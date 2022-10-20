package org.nutz.walnut.ext.media.ooml.explain.bean;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.cheap.dom.CheapElement;
import org.nutz.walnut.util.Ws;

public abstract class OEItem {

    protected OENodeType type;

    protected CheapElement refer;

    public abstract void renderTo(CheapElement pEl, NutBean vars);

    public void joinTrace(StringBuilder sb, int depth) {
        String prefix = Ws.repeat("|   ", depth);
        sb.append(prefix);
        sb.append("|-- ");
        sb.append(this.toString());
    }

    public String toString() {
        String name = Ws.camelCase(type.toString());
        name = Ws.upperFirst(name);
        if (this.hasReferElement()) {
            String rs = refer.toString();
            return String.format("{ %s } : %s", rs);
        }
        return String.format("{ %s }", name);
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

}
