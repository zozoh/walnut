package com.site0.walnut.util.tmpl.segment;

import java.util.List;

import org.nutz.lang.util.Callback2;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;
import com.site0.walnut.util.tmpl.ele.TmplEle;

public interface TmplSegment {

    void renderTo(WnTmplRenderContext rc);

    boolean isEnable(NutBean context);

    boolean isCanAddChild();

    boolean isCanAcceptElement();

    void addChild(TmplSegment child);

    void addElement(TmplEle ele);

    void eachElement(Callback2<Integer, TmplEle> callback);

    void eachDynamicElement(Callback2<Integer, TmplEle> callback);

    void joinDynamicElements(List<TmplEle> list);

    List<TmplEle> getDynamicElements();

    void joinDebugTree(StringBuilder sb, int indent);

}