package org.nutz.walnut.util.tmpl.segment;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.tmpl.ele.TmplEle;

public abstract class AbstractTmplSegment implements TmplSegment {

    protected List<TmplSegment> children;

    public AbstractTmplSegment() {
        this.children = new LinkedList<>();
    }

    @Override
    public boolean isEnable(NutBean context) {
        return true;
    }

    @Override
    public void renderTo(NutBean context, boolean showKey, StringBuilder sb) {
        if (null != children) {
            for (TmplSegment seg : children) {
                seg.renderTo(context, showKey, sb);
            }
        }
    }

    @Override
    public boolean isCanAddChild() {
        return true;
    }

    @Override
    public boolean isCanAcceptElement() {
        return false;
    }

    @Override
    public void addElement(TmplEle ele) {
        throw Wlang.noImplement();
    }

    @Override
    public void addChild(TmplSegment seg) {
        children.add(seg);
    }

    @Override
    public void joinDebugTree(StringBuilder sb, int indent) {
        if (indent > 0) {
            sb.append(Ws.repeat("|   ", indent));
        }
        sb.append('<').append(this.toTypeName()).append('>');
        if (null != this.children) {
            for (TmplSegment child : children) {
                sb.append('\n');
                child.joinDebugTree(sb, indent + 1);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinDebugTree(sb, 0);
        return sb.toString();
    }

    private String toTypeName() {
        String name = this.getClass().getSimpleName();
        int pos = name.indexOf("TmplSegment");
        if (pos > 0) {
            return name.substring(0, pos);
        }
        return name;
    }

}
