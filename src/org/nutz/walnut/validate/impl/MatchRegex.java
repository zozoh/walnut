package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnValidator;

public class MatchRegex implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val || args.length == 0) {
            return false;
        }

        // 任意统配
        if (null == args || args.length == 0 || ".*".equals(args[0])) {
            return true;
        }

        String as = args[0].toString();

        // Not
        boolean not = false;
        if (as.startsWith("!")) {
            not = true;
            as = as.substring(1).trim();
        }

        String str = val.toString();

        return str.matches(as) ^ not;
    }

}
