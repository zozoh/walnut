package com.site0.walnut.alg.exp.op;

import com.site0.walnut.alg.exp.WnExpOperator;
import com.site0.walnut.alg.rpn.RpnOperator;

public abstract class AbstractWnExpOperator implements WnExpOperator {

    protected String token;

    protected char name;

    protected int priority;

    public AbstractWnExpOperator(String token, char op, int pri) {
        this.token = token;
        this.name = op;
        this.priority = pri;
    }

    public String toString() {
        return this.token;
    }

    @Override
    public boolean isHigherPriority(RpnOperator op) {
        return this.comparePriority(op) > 0;
    }

    @Override
    public boolean isLowerPriority(RpnOperator op) {
        return this.comparePriority(op) < 0;
    }

    @Override
    public boolean isSamePriority(RpnOperator op) {
        return this.comparePriority(op) == 0;
    }

    @Override
    public int comparePriority(RpnOperator op) {
        return priority - op.getPriority();
    }

    @Override
    public int getPriority() {
        return priority;
    }

}
