package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnValidator;

public class MatchRegex implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val || args.length == 0) {
            return false;
        }
        String s = val.toString();

        return s.matches(args[0].toString());
    }

}
