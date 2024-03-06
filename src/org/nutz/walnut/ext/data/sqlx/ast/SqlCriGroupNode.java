package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

public class SqlCriGroupNode extends SqlCriteriaNode {

    private SqlCriteriaNode headNode;

    public boolean hasMultiChildren() {
        return null != headNode && headNode.hasNextNode();
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        if (this.hasMultiChildren()) {
            sb.append('(');
            headNode.joinTmpl(sb, useParams);
            sb.append(')');
        }
    }

    @Override
    public void joinParams(List<Object> params) {
        if (null != headNode) {
            headNode.joinParams(params);
        }
    }

    public SqlCriteriaNode getHeadNode() {
        return headNode;
    }

    public void setHeadNode(SqlCriteriaNode headNode) {
        this.headNode = headNode;
    }

}
