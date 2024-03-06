package org.nutz.walnut.util.tmpl.ele;

import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public class TmplStaticEle implements TmplEle {

    private String str;

    public TmplStaticEle(String str) {
        this.str = str;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public String getContent() {
        return str;
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        rc.sb.append(str);
    }

    @Override
    public String toString() {
        return str.replace("$", "$$");
    }

}
