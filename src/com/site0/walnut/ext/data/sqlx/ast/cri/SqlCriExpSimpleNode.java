package com.site0.walnut.ext.data.sqlx.ast.cri;

import java.util.List;

import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;

public abstract class SqlCriExpSimpleNode extends SqlCriExpressionNode {

    private Object value;

    private String operator;

    public SqlCriExpSimpleNode(String name, String opt, Object val) {
        super(name);
        this.value = val;
        this.operator = opt;
    }

    @Override
    protected void _join_self_params(List<SqlParam> params) {
        params.add(new SqlParam(this.getName(), value, null));
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        // 采用语句参数
        if (useParams) {
            sb.append(this.getName());
            sb.append(operator);
            sb.append("?");
        }
        // 采用普通语句
        else {
            String vs = Sqlx.valueToSqlExp(this.value);
            sb.append(this.getName());
            sb.append(operator);
            sb.append(vs);
        }
    }
}
