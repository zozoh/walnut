package org.nutz.walnut.ext.data.sqlx.tmpl.vars;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.ext.data.sqlx.ast.SqlCriteria;
import org.nutz.walnut.ext.data.sqlx.ast.SqlCriteriaNode;
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
        SqlCriteriaNode cri = SqlCriteria.toCriNode(bean);

        // 记入动态参数
        if (null != src && null != src.params) {
            cri.joinTmpl(src.out, true);
            cri.joinParams(src.params);
        }
        // 采用传统的 SQL 方式
        else {
            cri.joinTmpl(rc.out, false);
        }
    }

}
