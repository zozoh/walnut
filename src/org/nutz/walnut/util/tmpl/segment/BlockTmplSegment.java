package org.nutz.walnut.util.tmpl.segment;

import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.Wlang;
import org.nutz.walnut.util.Ws;
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

//    public String render(NutBean context) {
//        return render(context, true);
//    }
//
//    public String render(NutBean context, boolean showKey) {
//        if (null == context) {
//            context = new NutMap();
//        }
//        StringBuilder sb = new StringBuilder();
//        renderTo(context, showKey, sb);
//        return sb.toString();
//    }

    public void renderTo(NutBean context, StringBuilder sb) {
        this.renderTo(context, true, sb);
    }

    @Override
    public void renderTo(NutBean context, boolean showKey, StringBuilder sb) {
        for (TmplEle ele : elements) {
            ele.join(sb, context, showKey);
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
        for (TmplEle ele : elements) {
            ele.join(sb, null, true);
        }

    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.joinDebugTree(sb, 0);
        return sb.toString();
    }

}
