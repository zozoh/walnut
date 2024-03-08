package org.nutz.walnut.ext.data.sqlx.ast.cri;

import java.util.List;

import org.nutz.walnut.ext.data.sqlx.tmpl.SqlCriParam;

public class SqlCriExpNullNode extends SqlCriExpressionNode {

    public SqlCriExpNullNode(String name) {
        super(name);
    }

    @Override
    protected void _join_self_params(List<SqlCriParam> params) {}

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        sb.append(this.name).append(" IS NULL");
    }

}
