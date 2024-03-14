package com.site0.walnut.util.bean.util;

import com.site0.walnut.util.validate.WnMatch;
import com.site0.walnut.util.validate.impl.AutoMatch;

public class WnBeanFieldMatchValue {

    private Object test;

    private Object value;

    private WnMatch _m;

    public boolean matchTest(Object input) {
        if (null == test) {
            return true;
        }
        if (null == _m) {
            _m = AutoMatch.parse(test);
        }
        return _m.match(input);
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

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

}
