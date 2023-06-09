package org.nutz.walnut.util.tmpl.segment;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;

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

    public void addSegment(TmplSegment seg) {
        children.add(seg);
    }

}
