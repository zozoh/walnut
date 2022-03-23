package org.nutz.walnut.util.validate.impl;

import java.util.Collection;

import org.nutz.walnut.util.validate.WnMatch;

public class ParallelMatch implements WnMatch {

    private boolean defaultMatch;

    private WnMatch[] ms;

    public ParallelMatch(Collection<? extends WnMatch> mlist) {
        this(mlist, false);
    }

    public ParallelMatch(Collection<? extends WnMatch> mlist, boolean dft) {
        this.ms = new WnMatch[mlist.size()];
        mlist.toArray(this.ms);
        this.defaultMatch = dft;
    }

    public ParallelMatch(WnMatch... ms) {
        this.ms = ms;
    }

    @Override
    public boolean match(Object val) {
        if (null == ms || ms.length == 0)
            return defaultMatch;

        for (WnMatch m : ms) {
            if (m.match(val))
                return true;
        }

        return false;
    }

    public boolean isDefaultMatch() {
        return defaultMatch;
    }

    public void setDefaultMatch(boolean defaultMatch) {
        this.defaultMatch = defaultMatch;
    }

}
