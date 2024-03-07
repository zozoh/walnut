package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

import org.nutz.walnut.ext.data.sqlx.tmpl.WnSqls;

public class SqlCriExpLikeNode extends SqlCriExpressionNode {

    private String like;

    public SqlCriExpLikeNode(String name, String like) {
        super(name);
        this.like = like;
    }

    @Override
    public void joinParams(List<Object> params) {
        params.add(this.like);
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
                sb.append(" LIKE ?");
            }
        }
        // 采用普通语句
        else {
            String vs = WnSqls.valueToSqlExp(this.like);
            // 采用数据特殊方言
            if (null != this.dialect) {
                this.dialect.joinRegexp(sb, this.name, vs);
            }
            // 采用标准写法
            else {
                sb.append(this.name);
                sb.append(" LIKE ");
                sb.append(vs);
            }
        }
    }
    
}
