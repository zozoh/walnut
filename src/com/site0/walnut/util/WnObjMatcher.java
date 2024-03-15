package com.site0.walnut.util;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class WnObjMatcher {

    private WnMatch wm;

    public WnObjMatcher() {}

    public WnObjMatcher setf(String fmt, Object... args) {
        return set(Wlang.mapf(fmt, args));
    }

    public WnObjMatcher set(Object input) {
        this.wm = new AutoMatch(input);
        return this;
    }

    public boolean match(WnObj o) {
        if (null == wm) {
            return false;
        }
        return wm.match(o);
    }
}
