package org.nutz.walnut.ext.data.sqlx.ast;

import java.util.List;

import org.nutz.walnut.ext.data.sqlx.expert.SqlDialect;
import org.nutz.walnut.ext.data.sqlx.tmpl.SqlParam;

public abstract class SqlCriteriaNode {

    protected SqlDialect dialect;

    private boolean not;

    private SqlCriteriaNode parent;

    private SqlCriteriaNode prevNode;

    private SqlCriteriaNode nextNode;

    private SqlCriJoin nextJoin;

    protected abstract void _join_self_params(List<SqlParam> params);

    protected abstract void _join_self(StringBuilder sb, boolean useParams);

    public void joinParams(List<SqlParam> params) {
        _join_self_params(params);
        if (null != nextNode) {
            nextNode.joinParams(params);
        }
    }

    public void joinTmpl(StringBuilder sb, boolean useParams) {
        if (this.not) {
            sb.append("NOT ");
        }
        joinSelfAndNext(sb, useParams);
    }

    public String toString() {
        return this.toSql(false);
    }

    public String toSql(boolean useParmas) {
        StringBuilder sb = new StringBuilder();
        this.joinTmpl(sb, useParmas);
        return sb.toString();
    }

    public void joinSelfAndNext(StringBuilder sb, boolean useParams) {
        this._join_self(sb, useParams);
        if (null != nextJoin && null != nextNode) {
            sb.append(' ').append(nextJoin.name()).append(' ');
            nextNode.joinTmpl(sb, useParams);
        }
    }

    public boolean isNot() {
        return not;
    }

    public SqlCriteriaNode setNot(boolean not) {
        this.not = not;
        return this;
    }

    public void toggleNot() {
        this.not = !this.not;
    }

    public void and(SqlCriteriaNode next) {
        this.nextJoin = SqlCriJoin.AND;
        this.setNextNode(next);
    }

    public void or(SqlCriteriaNode next) {
        this.nextJoin = SqlCriJoin.OR;
        this.setNextNode(next);
    }

    public boolean hasNextNode() {
        return null != nextNode && null != nextJoin;
    }

    public SqlCriteriaNode getNextNode() {
        return nextNode;
    }

    public void setNextNode(SqlCriteriaNode next) {
        this.nextNode = next;
        if (next != null) {
            next.dialect = this.dialect;
            next.prevNode = this;
            next.parent = this.parent;
        }
    }

    public SqlCriJoin getNextJoin() {
        return nextJoin;
    }

    public void setNextJoin(SqlCriJoin nextJoin) {
        this.nextJoin = nextJoin;
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    public void setDialect(SqlDialect expert) {
        this.dialect = expert;
    }

    public boolean hasPrev() {
        return null != prevNode;
    }

    public boolean hasParent() {
        return null != parent;
    }

    public SqlCriteriaNode getParent() {
        return parent;
    }

    public void setParent(SqlCriteriaNode parent) {
        this.parent = parent;
    }

}
