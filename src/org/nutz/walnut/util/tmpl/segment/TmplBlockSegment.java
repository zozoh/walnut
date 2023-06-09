package org.nutz.walnut.util.tmpl.segment;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.tmpl.ele.TmplEle;

public class TmplBlockSegment implements TmplSegment {

    protected List<TmplEle> elements;

    public TmplBlockSegment() {
        elements = new LinkedList<>();
    }

    public TmplBlockSegment(List<TmplEle> eles) {
        this();
        this.addElements(eles);
    }

    @Override
    public boolean isEnable(NutBean context) {
        return true;
    }

    public void addElement(TmplEle ele) {
        elements.add(ele);
    }

    public void addElements(List<TmplEle> eles) {
        elements.addAll(eles);
    }

    public String render(NutBean context) {
        return render(context, true);
    }

    public String render(NutBean context, boolean showKey) {
        if (null == context) {
            context = new NutMap();
        }
        StringBuilder sb = new StringBuilder();
        renderTo(context, showKey, sb);
        return sb.toString();
    }

    public void renderTo(NutBean context, StringBuilder sb) {
        this.renderTo(context, true, sb);
    }

    @Override
    public void renderTo(NutBean context, boolean showKey, StringBuilder sb) {
        for (TmplEle ele : elements) {
            ele.join(sb, context, showKey);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TmplEle ele : elements) {
            sb.append(ele);
        }
        return sb.toString();
    }

}
