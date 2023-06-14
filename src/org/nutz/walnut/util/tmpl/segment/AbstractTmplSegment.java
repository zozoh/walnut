package org.nutz.walnut.util.tmpl.segment;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Wlang;
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
    public String toString() {
        String name = this.getClass().getSimpleName();
        int pos = name.indexOf("TmplSegment");
        return name.substring(0, pos);
    }

}
