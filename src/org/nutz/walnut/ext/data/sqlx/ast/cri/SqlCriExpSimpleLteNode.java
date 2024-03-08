package org.nutz.walnut.ext.data.sqlx.ast.cri;

public class SqlCriExpSimpleLteNode extends SqlCriExpSimpleNode {

    public SqlCriExpSimpleLteNode(String name, Object val) {
        super(name, "<=", val);
    }

}
