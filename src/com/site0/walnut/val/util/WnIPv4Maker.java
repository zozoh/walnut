package com.site0.walnut.val.util;

import java.util.Date;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Wn;
import com.site0.walnut.val.ValueMaker;

public class WnIPv4Maker implements ValueMaker {

    @Override
    public Object make(Date hint, NutBean context) {
        return Wn.WC().getIPv4();
    }

}
