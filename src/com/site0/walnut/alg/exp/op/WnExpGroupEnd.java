package com.site0.walnut.alg.exp.op;

import com.site0.walnut.alg.rpn.RpnGroupEnd;

public class WnExpGroupEnd extends AbstractWnExpOperator implements RpnGroupEnd {

    public WnExpGroupEnd(String token, char op, int pri) {
        super(token, op, pri);
    }

}
