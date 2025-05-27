package com.site0.walnut.ext.data.sqlx.ast.cri;

import com.site0.walnut.ext.data.sqlx.ast.SqlCriteriaNode;
import com.site0.walnut.util.Ws;

public abstract class SqlCriExpressionNode extends SqlCriteriaNode {

    private String name;

    private String fieldPrefix;

    protected SqlCriExpressionNode(String name) {
        this.name = name;
    }

    @Override
    public void setFieldPrefix(String fieldPrefix) {
        this.fieldPrefix = fieldPrefix;
        super.setFieldPrefix(fieldPrefix);
    }

    public String getName() {
        if (null != this.fieldPrefix) {
            return this.fieldPrefix + this.name;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isEmpty() {
        return Ws.isBlank(name);
    }

}