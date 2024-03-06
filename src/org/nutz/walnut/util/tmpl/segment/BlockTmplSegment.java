package org.nutz.walnut.util.tmpl.segment;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.Callback2;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.tmpl.WnTmplRenderContext;
import org.nutz.walnut.util.tmpl.ele.TmplEle;

public class BlockTmplSegment implements TmplSegment {

    protected List<TmplEle> elements;

    public BlockTmplSegment() {
        elements = new LinkedList<>();
    }

    public BlockTmplSegment(List<TmplEle> eles) {
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

    @Override
    public void eachElement(Callback2<Integer, TmplEle> callback) {
        if (null != elements) {
            int i = 0;
            for (TmplEle ele : elements) {
                callback.invoke(i++, ele);
            }
        }
    }

    @Override
    public void eachDynamicElement(Callback2<Integer, TmplEle> callback) {
        if (null != elements) {
            int i = 0;
            for (TmplEle ele : elements) {
                if (ele.isDynamic()) {
                    callback.invoke(i++, ele);
                }
            }
        }
    }

    @Override
    public void joinDynamicElements(List<TmplEle> list) {
        if (null != elements) {
            for (TmplEle ele : elements) {
                if (ele.isDynamic()) {
                    list.add(ele);
                }
            }
        }
    }

    @Override
    public List<TmplEle> getDynamicElements() {
        List<TmplEle> list = new LinkedList<>();
        this.joinDynamicElements(list);
        return list;
    }

    public void renderTo(NutBean context, StringBuilder sb) {
        WnTmplRenderContext rc = new WnTmplRenderContext(sb, context);
        this.renderTo(rc);
    }

    @Override
    public void renderTo(WnTmplRenderContext rc) {
        for (TmplEle ele : elements) {
            ele.join(rc);
        }
    }

    @Override
    public boolean isCanAddChild() {
        return false;
    }

    @Override
    public boolean isCanAcceptElement() {
        return true;
    }

    @Override
    public void addChild(TmplSegment child) {
        throw Wlang.noImplement();
    }

    @Override
    public void joinDebugTree(StringBuilder sb, int indent) {
        if (indent > 0) {
            sb.append(Ws.repeat("|   ", indent));
        }
        sb.append("<Block>");
        WnTmplRenderContext rc = new WnTmplRenderContext(sb, true);
        for (TmplEle ele : elements) {
            ele.join(rc);
        }

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinDebugTree(sb, 0);
        return sb.toString();
    }

}
