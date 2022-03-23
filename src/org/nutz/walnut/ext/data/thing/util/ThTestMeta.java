package org.nutz.walnut.ext.data.thing.util;

import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class ThTestMeta {

    private Object test;

    private NutMap meta;

    private WnMatch _m;

    public boolean isMatch(NutBean bean) {
        if (null == _m) {
            _m = AutoMatch.parse(test, true);
        }
        return _m.match(bean);
    }

    public boolean hasTest() {
        return null != test;
    }

    public Object getTest() {
        return test;
    }

    public void setTest(Object test) {
        this.test = test;
    }

    public boolean hasMeta() {
        return null != meta && !meta.isEmpty();
    }

    public NutMap getMeta() {
        return meta;
    }

    public void setMeta(NutMap meta) {
        this.meta = meta;
    }

}
