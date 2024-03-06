package org.nutz.walnut.ext.data.sqlx.tmpl.vars;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.sqlx.tmpl.SqlRenderContext;
import org.nutz.walnut.util.tmpl.WnTmplRenderContext;

public class VarsAsWhereElement extends SqlVarsElement {

    public VarsAsWhereElement(String content) {
        super(content);
    }

    @Override
    public void join(WnTmplRenderContext rc) {
        SqlRenderContext src = null;
        if (rc instanceof SqlRenderContext) {
            src = (SqlRenderContext) rc;
        }
        NutBean bean = this.getBean(rc.context);
    }

}
