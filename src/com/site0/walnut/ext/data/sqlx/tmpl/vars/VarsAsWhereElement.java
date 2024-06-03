package com.site0.walnut.ext.data.sqlx.tmpl.vars;

import com.site0.walnut.ext.data.sqlx.ast.SqlCriteria;
import com.site0.walnut.ext.data.sqlx.ast.SqlCriteriaNode;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlRenderContext;
import com.site0.walnut.util.tmpl.WnTmplRenderContext;

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
        Object input = this.getObject(rc.context);
        SqlCriteriaNode cri = SqlCriteria.toCriNode(input);

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
