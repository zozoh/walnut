package org.nutz.walnut.validate.impl;

import java.util.Collection;

import org.nutz.walnut.validate.WnMatch;

public class ParallelMatch implements WnMatch {

    private WnMatch[] ms;

    public ParallelMatch(Collection<? extends WnMatch> mlist) {
        this.ms = new WnMatch[mlist.size()];
        mlist.toArray(this.ms);
    }

    public ParallelMatch(WnMatch... ms) {
        this.ms = ms;
    }

    @Override
    public boolean match(Object val) {
        if (null == ms)
            return false;

        for (WnMatch m : ms) {
            if (m.match(val))
                return true;
        }

        return false;
    }

}
