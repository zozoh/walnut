package org.nutz.walnut.alg.exp.op;

import org.nutz.walnut.alg.rpn.RpnGroupEnd;

public class WnExpGroupEnd extends AbstractWnExpOperator implements RpnGroupEnd {

    public WnExpGroupEnd(String token, char op, int pri) {
        super(token, op, pri);
    }

}
