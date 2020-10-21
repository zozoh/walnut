package org.nutz.walnut.validate.impl;

import java.util.Collection;

import org.nutz.walnut.validate.WnMatch;

/**
 * 根据一组输入的字符串进行匹配
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class AutoMultiStrMatch implements WnMatch {

    /**
     * true 表示所有条件为真才为真，否则一个为真即为真
     */
    private boolean and;

    private WnMatch[] ms;

    public AutoMultiStrMatch(String[] strs) {
        ms = new WnMatch[strs.length];
        for (int i = 0; i < strs.length; i++) {
            ms[i] = new AutoStrMatch(strs[i]);
        }
    }

    public AutoMultiStrMatch(Collection<? extends CharSequence> coll) {
        ms = new WnMatch[coll.size()];
        int i = 0;
        for (CharSequence cs : coll) {
            ms[i++] = new AutoStrMatch(cs);
        }
    }

    public boolean isAnd() {
        return and;
    }

    public void setAnd(boolean and) {
        this.and = and;
    }

    public AutoMultiStrMatch asAnd() {
        this.and = true;
        return this;
    }

    public AutoMultiStrMatch asOr() {
        this.and = false;
        return this;
    }

    public WnMatch[] getMs() {
        return ms;
    }

    public void setMs(WnMatch[] ms) {
        this.ms = ms;
    }

    @Override
    public boolean match(Object val) {
        // 全部匹配
        if (and) {
            for (WnMatch m : ms) {
                if (!m.match(val)) {
                    return false;
                }
            }
            return true;
        }
        // 部分匹配
        for (WnMatch m : ms) {
            if (m.match(val)) {
                return true;
            }
        }
        return false;
    }

}
