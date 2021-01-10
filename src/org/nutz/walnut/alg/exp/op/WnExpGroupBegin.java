package org.nutz.walnut.alg.exp.op;

import org.nutz.walnut.alg.rpn.RpnGroupBegin;

public class WnExpGroupBegin extends AbstractWnExpOperator implements RpnGroupBegin {

    public WnExpGroupBegin(String token, char op, int pri) {
        super(token, op, pri);
    }

}
