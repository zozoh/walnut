package org.nutz.walnut.util.tmpl.ele;

import org.nutz.lang.util.NutBean;

public interface TmplEle {

    void join(StringBuilder sb, NutBean context, boolean showKey);

}
