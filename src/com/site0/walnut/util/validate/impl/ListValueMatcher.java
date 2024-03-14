package com.site0.walnut.util.validate.impl;

import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.each.WnBreakException;
import com.site0.walnut.util.each.WnEachIteratee;
import com.site0.walnut.util.validate.WnMatch;

public abstract class ListValueMatcher implements WnMatch {

    /**
     * 如果输入的值是数组（列表），是否需要全部匹配
     */
    protected boolean matchAll;

    protected abstract boolean __match_val(Object val);

    @Override
    public boolean match(Object val) {
        if (null == val)
            return false;

        boolean[] re = new boolean[1];
        re[0] = matchAll;

        Wlang.each(val, new WnEachIteratee<Object>() {
            public void invoke(int index, Object ele, Object src) throws WnBreakException {
                // 全部匹配的话，找不匹配
                if (matchAll) {
                    if (!__match_val(ele)) {
                        re[0] = false;
                        throw new WnBreakException();
                    }
                }
                // 否则一个匹配上就人生赢家了
                else {
                    if (__match_val(ele)) {
                        re[0] = true;
                        throw new WnBreakException();
                    }
                }
            }

        });

        return re[0];
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }
}
