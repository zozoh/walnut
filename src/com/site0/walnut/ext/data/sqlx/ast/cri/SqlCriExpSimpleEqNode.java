package com.site0.walnut.ext.data.sqlx.ast.cri;

public class SqlCriExpSimpleEqNode extends SqlCriExpSimpleNode {

    public SqlCriExpSimpleEqNode(String name, Object val) {
        super(name, "=", val);
    }

}
