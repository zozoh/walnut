package com.site0.walnut.util.tmpl.ele;

import com.site0.walnut.util.tmpl.WnTmplRenderContext;

public interface TmplEle {

    boolean isDynamic();

    String getContent();

    void join(WnTmplRenderContext rc);

}
