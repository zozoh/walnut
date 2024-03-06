package org.nutz.walnut.util.tmpl.ele;

import org.nutz.lang.util.NutBean;

public interface TmplEle {
    
    boolean isDynamic();
    
    String getContent();

    void join(StringBuilder sb, NutBean context, boolean showKey);

}
