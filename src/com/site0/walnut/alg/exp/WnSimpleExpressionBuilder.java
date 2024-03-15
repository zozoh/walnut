package com.site0.walnut.alg.exp;

import java.util.HashMap;
import java.util.List;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.alg.exp.op.*;
import com.site0.walnut.alg.exp.val.*;

public class WnSimpleExpressionBuilder extends AbstractWnExpressBuilder {

    public WnSimpleExpressionBuilder() {
        this.opTable = new WnCharOpTable("+-", "*/", "()");
        this.opMapping = new HashMap<>();
        opMapping.put("+", '+');
        opMapping.put("-", '-');
        opMapping.put("*", '*');
        opMapping.put("/", '/');
        opMapping.put("(", '(');
        opMapping.put(")", ')');
    }

    @Override
    protected WnExpOperator createOperator(String token, char op, int priority) {
        if ('(' == op) {
            return new WnExpGroupBegin(token, op, priority);
        }
        if (')' == op) {
            return new WnExpGroupEnd(token, op, priority);
        }
        if ('+' == op) {
            return new WnExpOpAdd(token, op, priority);
        }
        if ('-' == op) {
            return new WnExpOpSub(token, op, priority);
        }
        if ('*' == op) {
            return new WnExpOpMul(token, op, priority);
        }
        if ('/' == op) {
            return new WnExpOpDiv(token, op, priority);
        }
        throw Wlang.impossible();
    }

    @Override
    protected WnExpValue createOperand(String token) {
        if (token.matches("^\\d+$")) {
            return new WnExpValInt(token);
        }
        if (token.matches("^\\d*\\.\\d+$")) {
            return new WnExpValFloat(token);
        }
        throw Wlang.impossible();
    }

    @Override
    protected List<String> prepareTokens(List<String> tokens) {
        return tokens;
    }

}
