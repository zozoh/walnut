package com.site0.walnut.ext.data.sqlx.ast.cri;

import java.util.List;

import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.ext.data.sqlx.util.Sqlx;

public class SqlCriExpLikeNode extends SqlCriExpressionNode {

    private String like;

    public SqlCriExpLikeNode(String name, String like) {
        super(name);
        this.like = like;
    }

    @Override
    protected void _join_self_params(List<SqlParam> params) {
        params.add(new SqlParam(this.getName(), like, null));
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        // 采用语句参数
        if (useParams) {
            // 采用数据特殊方言
            if (null != this.dialect) {
                this.dialect.joinRegexp(sb, this.getName(), "?");
            }
            // 采用标准写法
            else {
                sb.append(this.getName());
                sb.append(" LIKE ?");
            }
        }
        // 采用普通语句
        else {
            String vs = Sqlx.valueToSqlExp(this.like);
            // 采用数据特殊方言
            if (null != this.dialect) {
                this.dialect.joinRegexp(sb, this.getName(), vs);
            }
            // 采用标准写法
            else {
                sb.append(this.getName());
                sb.append(" LIKE ");
                sb.append(vs);
            }
        }
    }

}
