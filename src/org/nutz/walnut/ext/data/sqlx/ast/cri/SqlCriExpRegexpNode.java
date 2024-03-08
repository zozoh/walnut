package org.nutz.walnut.ext.data.sqlx.ast.cri;

import java.util.List;

import org.nutz.walnut.ext.data.sqlx.tmpl.SqlCriParam;
import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqls;

public class SqlCriExpRegexpNode extends SqlCriExpressionNode {

    private String regex;

    public SqlCriExpRegexpNode(String name, String regex) {
        super(name);
        this.regex = regex;
    }

    @Override
    protected void _join_self_params(List<SqlCriParam> params) {
        params.add(new SqlCriParam(name, regex));
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        // 采用语句参数
        if (useParams) {
            // 采用数据特殊方言
            if (null != this.dialect) {
                this.dialect.joinRegexp(sb, this.name, "?");
            }
            // 采用标准写法
            else {
                sb.append(this.name);
                sb.append(" REGEXP ?");
            }
        }
        // 采用普通语句
        else {
            String vs = WnSqls.valueToSqlExp(this.regex);
            // 采用数据特殊方言
            if (null != this.dialect) {
                this.dialect.joinRegexp(sb, this.name, vs);
            }
            // 采用标准写法
            else {
                sb.append(this.name);
                sb.append(" REGEXP ");
                sb.append(vs);
            }
        }
    }

}
