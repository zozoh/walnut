package org.nutz.walnut.util.tmpl.segment;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.util.tmpl.ele.TmplEle;

public interface TmplSegment {

    void renderTo(NutBean context, boolean showKey, StringBuilder sb);

    boolean isEnable(NutBean context);

    boolean isCanAddChild();

    boolean isCanAcceptElement();

    void addChild(TmplSegment child);

    void addElement(TmplEle ele);

    void joinDebugTree(StringBuilder sb, int indent);

}