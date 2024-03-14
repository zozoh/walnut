package com.site0.walnut.ext.data.sqlx.ast.cri;

import com.site0.walnut.ext.data.sqlx.ast.SqlCriteriaNode;

public abstract class SqlCriExpressionNode extends SqlCriteriaNode {

    protected String name;

    protected SqlCriExpressionNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}