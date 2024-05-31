package com.site0.walnut.val;

import java.util.Date;

import org.nutz.lang.util.NutBean;

public class ContextValueMaker implements ValueMaker {

    private String key;

    public ContextValueMaker(String key) {
        this.key = key;
    }

    @Override
    public Object make(Date hint, NutBean context) {
        return context.get(key);
    }

}
