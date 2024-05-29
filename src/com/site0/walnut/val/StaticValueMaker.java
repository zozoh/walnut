package com.site0.walnut.val;

import java.util.Date;

import org.nutz.lang.util.NutBean;

public class StaticValueMaker implements ValueMaker {

    private Object val;

    public StaticValueMaker(Object val) {
        this.val = val;
    }

    @Override
    public Object make(Date hint, NutBean context) {
        return this.val;
    }
}
