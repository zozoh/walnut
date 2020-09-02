package org.nutz.walnut.validate.match;

import org.nutz.walnut.validate.WnMatch;

public class WildcardMatch implements WnMatch {

    private boolean not;

    private String sub;

    /**
     * -1: 前置(endsWith), 1 后置(startsWith), 0 精确
     */
    private int position;

    public WildcardMatch(String wildcard) {
        if (wildcard.startsWith("!")) {
            not = true;
            wildcard = wildcard.substring(1);
        }
        // 前置
        if (wildcard.startsWith("*")) {
            position = -1;
            sub = wildcard.substring(1);
        }
        // 后置
        else if (wildcard.endsWith("*")) {
            position = 1;
            sub = wildcard.substring(0, wildcard.length() - 1);
        }
        // 精确
        else {
            position = 0;
            sub = wildcard;
        }
    }

    @Override
    public boolean match(Object val) {
        if (null == val) {
            return false;
        }
        String str = val.toString();

        // 前置
        if (-1 == position) {
            return str.endsWith(sub) ^ not;
        }

        // 后置
        if (1 == position) {
            return str.startsWith(sub) ^ not;
        }

        // 精确
        return str.equals(sub) ^ not;
    }

}
