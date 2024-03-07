package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

public class SqlCriExpNullNode extends SqlCriExpressionNode {

    public SqlCriExpNullNode(String name) {
        super(name);
    }

    @Override
    public void joinParams(List<Object> params) {}

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        sb.append(this.name).append(" IS NULL");
    }

}
