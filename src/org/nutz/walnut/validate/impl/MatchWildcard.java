package org.nutz.walnut.validate.impl;

import org.nutz.walnut.validate.WnValidator;

public class MatchWildcard implements WnValidator {

    @Override
    public boolean isTrue(Object val, Object[] args) {
        if (null == val)
            return false;

        String str = val.toString();

        // 任意统配
        if (null == args || args.length == 0 || "*".equals(args[0])) {
            return true;
        }

        String as = args[0].toString();

        // Not
        boolean not = false;
        if (as.startsWith("!")) {
            not = true;
            as = as.substring(1).trim();
        }

        // 前置
        if (as.startsWith("*")) {
            String s = as.substring(1).trim();
            return str.endsWith(s) ^ not;
        }

        // 后置
        if (as.endsWith("*")) {
            String s = as.substring(0, as.length() - 1).trim();
            return str.startsWith(s) ^ not;
        }

        // 精确
        return str.equals(as) ^ not;
    }

}
