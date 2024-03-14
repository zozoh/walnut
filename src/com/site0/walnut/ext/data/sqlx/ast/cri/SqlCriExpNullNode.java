package com.site0.walnut.ext.data.sqlx.ast.cri;

import java.util.List;

import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;

public class SqlCriExpNullNode extends SqlCriExpressionNode {

    public SqlCriExpNullNode(String name) {
        super(name);
    }

    @Override
    protected void _join_self_params(List<SqlParam> params) {}

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        sb.append(this.name).append(" IS NULL");
    }

}
