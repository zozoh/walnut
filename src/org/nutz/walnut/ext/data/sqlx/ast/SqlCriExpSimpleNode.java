package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqls;

public class SqlCriExpSimpleNode extends SqlCriExpressionNode {

    private Object value;

    public SqlCriExpSimpleNode(String name, Object val) {
        super(name);
        this.value = val;
    }

    @Override
    public void joinParams(List<Object> params) {
        params.add(this.value);
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        // 采用语句参数
        if (useParams) {
            sb.append(this.name);
            sb.append("=?");
        }
        // 采用普通语句
        else {
            String vs = WnSqls.valueToSqlExp(this.value);
            sb.append(this.name);
            sb.append('=');
            sb.append(vs);
        }
    }

}
