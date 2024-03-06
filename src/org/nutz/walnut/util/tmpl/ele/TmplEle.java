package org.nutz.walnut.util.tmpl.ele;

import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public interface TmplEle {

    boolean isDynamic();

    String getContent();

    void join(WnTmplRenderContext rc);

}
