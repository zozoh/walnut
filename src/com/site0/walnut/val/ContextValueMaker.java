package com.site0.walnut.val;

import java.util.Date;

import org.nutz.lang.util.NutBean;
import org.nutz.mapl.Mapl;

public class ContextValueMaker implements ValueMaker {

    private String key;

    public ContextValueMaker(String key) {
        this.key = key;
    }

    @Override
    public Object make(Date hint, NutBean context) {
        return Mapl.cell(context, key);
    }

}
