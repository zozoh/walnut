package com.site0.walnut.ext.data.sqlx.ast.cri;

import java.util.List;

import com.site0.walnut.ext.data.sqlx.ast.SqlCriJoin;
import com.site0.walnut.ext.data.sqlx.ast.SqlCriteriaNode;
import com.site0.walnut.ext.data.sqlx.tmpl.SqlParam;
import com.site0.walnut.util.Wlang;

public class SqlCriGroupNode extends SqlCriteriaNode {

    private SqlCriteriaNode headNode;

    public boolean hasMultiChildren() {
        return null != headNode && headNode.hasNextNode();
    }

    @Override
    public boolean isEmpty() {
        return null == headNode || headNode.isEmpty();
    }

    @Override
    protected void _join_self(StringBuilder sb, boolean useParams) {
        throw Wlang.noImplement();
    }

    private void __join_not(StringBuilder sb) {
        sb.append("NOT ");
    }

    @Override
    public void joinTmpl(StringBuilder sb, boolean useParams) {
        // 子组需要括弧
        if (this.hasParent() && this.hasMultiChildren()) {
            if (this.isNot()) {
                this.__join_not(sb);
            }
            sb.append('(');
            headNode.joinTmpl(sb, useParams);
            sb.append(')');
        }
        // 顶级组无需括弧，不过也不一定
        else if (null != headNode) {
            // NOT + NOT
            if (this.isNot()) {
                // 整体取反，那么自己需要扩一下
                if (this.hasMultiChildren()) {
                    this.__join_not(sb);
                    sb.append('(');
                    headNode.joinTmpl(sb, useParams);
                    sb.append(')');
                }
                // 只有一个表达式，也是反的？ NOT+NOT 咯
                else if (headNode.isNot()) {
                    headNode.joinSelfAndNext(sb, useParams);
                }
                // 那么直接用子，反正就它一个
                else {
                    this.__join_not(sb);
                    headNode.joinSelfAndNext(sb, useParams);
                }
            }
            // 正常
            else {
                headNode.joinTmpl(sb, useParams);
            }
        }

        if (this.hasNextNode()) {
            SqlCriJoin nextJoin = this.getNextJoin();
            SqlCriteriaNode nextNode = this.getNextNode();
            sb.append(' ').append(nextJoin.name()).append(' ');
            nextNode.joinTmpl(sb, useParams);
        }
    }

    @Override
    protected void _join_self_params(List<SqlParam> params) {
        if (null != headNode) {
            headNode.joinParams(params);
        }
    }

    public SqlCriteriaNode getHeadNode() {
        return headNode;
    }

    public void setHeadNode(SqlCriteriaNode headNode) {
        this.headNode = headNode;
        if (null != headNode) {
            headNode.setParent(this);
        }
    }

}
