package com.site0.walnut.ext.data.sqlx.ast.cri;

import java.util.Arrays;
import java.util.List;

import com.site0.walnut.ext.data.sqlx.tmpl.WnSqls;
import com.site0.walnut.util.Ws;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;

public class SqlCriExpEnumNode extends SqlCriExpressionNode {

    private Object[] vals;

    public SqlCriExpEnumNode(String name, Object[] vals) {
        super(name);
        this.vals = vals;
    }

    @Override
    protected void _join_self_params(List<SqlParam> params) {
        if (null != vals) {
            for (int i = 0; i < vals.length; i++) {
                Object v = vals[i];
                String k = name + "." + i;
                params.add(new SqlParam(k, v));
            }
        }
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        if (null == vals || vals.length == 0) {
            return;
        }
        // 采用语句参数
        if (useParams) {
            String[] vs = new String[vals.length];
            Arrays.fill(vs, "?");
            sb.append(this.name);
            sb.append(" IN (").append(Ws.join(vs, ",")).append(")");
        }
        // 采用普通语句
        else {
            String[] vs = new String[vals.length];
            for (int i = 0; i < vals.length; i++) {
                String s = WnSqls.valueToSqlExp(vals[i]);
                vs[i] = s;
            }
            sb.append(this.name);
            sb.append(" IN (").append(Ws.join(vs, ",")).append(")");
        }
    }

}