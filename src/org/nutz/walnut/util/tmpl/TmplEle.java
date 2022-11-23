package org.nutz.walnut.util.tmpl;

import org.nutz.lang.util.NutBean;

interface TmplEle {

    void join(StringBuilder sb, NutBean context, boolean showKey);

}
