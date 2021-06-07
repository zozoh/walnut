package org.nutz.walnut.util;

import org.nutz.lang.Lang;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class WnObjMatcher {

    private WnMatch wm;

    public WnObjMatcher() {}

    public WnObjMatcher setf(String fmt, Object... args) {
        return set(Lang.mapf(fmt, args));
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
