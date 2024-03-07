package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

import org.nutz.walnut.util.Wlang;

public abstract class SqlCriExpressionNode extends SqlCriteriaNode {

    protected String name;

    protected SqlCriExpressionNode(String name) {
        this.name = name;
    }

    @Override
    public void joinParams(List<Object> params) {
        throw Wlang.noImplement();
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        throw Wlang.noImplement();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}