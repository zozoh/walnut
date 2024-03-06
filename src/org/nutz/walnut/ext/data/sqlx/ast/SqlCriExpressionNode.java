package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

import org.nutz.walnut.util.Wlang;

public abstract class SqlCriExpressionNode extends SqlCriteriaNode {

    @Override
    public void joinParams(List<Object> params) {
        throw Wlang.noImplement();
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        throw Wlang.noImplement();
    }
}