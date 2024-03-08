package org.nutz.walnut.ext.data.sqlx.ast.cri;

public class SqlCriExpSimpleGteNode extends SqlCriExpSimpleNode {

    public SqlCriExpSimpleGteNode(String name, Object val) {
        super(name, ">=", val);
    }

}
