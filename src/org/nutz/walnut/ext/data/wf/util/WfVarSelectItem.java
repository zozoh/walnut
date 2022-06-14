package org.nutz.walnut.ext.data.wf.util;

import org.nutz.json.Json;
import org.nutz.walnut.util.validate.WnMatch;
import org.nutz.walnut.util.validate.impl.AutoMatch;

public class WfVarSelectItem {

    private Object test;

    private Object value;

    private WnMatch _m;

    public String toString() {
        return Json.toJson(this);
    }

    private WnMatch getMatch() {
        if (null == _m) {
            _m = AutoMatch.parse(test, true);
        }
        return _m;
    }

    public boolean isMatch(Object val) {
        return this.getMatch().match(val);
    }

    public Object getTest() {
        return test;
    }

    public void setTest(Object test) {
        this.test = test;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
