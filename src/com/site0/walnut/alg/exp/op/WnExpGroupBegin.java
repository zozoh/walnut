package com.site0.walnut.alg.exp.op;

import com.site0.walnut.alg.rpn.RpnGroupBegin;

public class WnExpGroupBegin extends AbstractWnExpOperator implements RpnGroupBegin {

    public WnExpGroupBegin(String token, char op, int pri) {
        super(token, op, pri);
    }

}
