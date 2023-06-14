package org.nutz.walnut.util.tmpl.segment;

import org.nutz.lang.util.NutBean;

public interface TmplSegment {

    void renderTo(NutBean context, boolean showKey, StringBuilder sb);

    boolean isEnable(NutBean context);
    
    boolean canAddChild();
    
    void addChild(TmplSegment child);

}