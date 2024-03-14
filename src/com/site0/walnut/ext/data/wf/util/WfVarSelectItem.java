package com.site0.walnut.ext.data.wf.util;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class WfVarSelectItem {

    private Object test;

    private NutMap value;

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

    public NutMap getValue() {
        return value;
    }

    public void setValue(NutMap value) {
        this.value = value;
    }

}
