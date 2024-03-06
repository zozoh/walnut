package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

public abstract class SqlCriteriaNode {

    private boolean not;

    private SqlCriteriaNode nextNode;

    private SqlCriJoin nextJoin;

    public abstract void joinParams(List<Object> params);

    protected abstract void _join_self(StringBuilder sb, boolean useParams);

    public void joinTmpl(StringBuilder sb, boolean useParams) {
        if (this.not) {
            sb.append(" NOT ");
        }
        this._join_self(sb, useParams);
        if (null != nextJoin && null != nextNode) {
            sb.append(' ').append(nextJoin.name()).append(' ');
            nextNode.joinTmpl(sb, useParams);
        }

    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public void toggleNot() {
        this.not = !this.not;
    }

    public boolean hasNextNode() {
        return null != nextNode;
    }

    public SqlCriteriaNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(SqlCriteriaNode nextNode) {
        this.nextNode = nextNode;
    }

    public SqlCriJoin getNextJoin() {
        return nextJoin;
    }

    public void setNextJoin(SqlCriJoin nextJoin) {
        this.nextJoin = nextJoin;
    }

}
