package org.nutz.walnut.ext.data.sqlx.ast.cri;

import java.util.List;

import org.nutz.walnut.ext.data.sqlx.tmpl.SqlCriParam;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqls;

public abstract class SqlCriExpSimpleNode extends SqlCriExpressionNode {

    private Object value;

    private String operator;

    public SqlCriExpSimpleNode(String name, String opt, Object val) {
        super(name);
        this.value = val;
        this.operator = opt;
    }

    @Override
    protected void _join_self_params(List<SqlCriParam> params) {
        params.add(new SqlCriParam(name, value));
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        // 采用语句参数
        if (useParams) {
            sb.append(this.name);
            sb.append(operator);
            sb.append("?");
        }
        // 采用普通语句
        else {
            String vs = WnSqls.valueToSqlExp(this.value);
            sb.append(this.name);
            sb.append(operator);
            sb.append(vs);
        }
    }
}
