package com.site0.walnut.util.bean.val;

import org.nutz.castor.Castors;
import com.site0.walnut.util.bean.WnEnumOptionItem;
import com.site0.walnut.util.bean.WnValue;

public class WnBooleanValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        int bi = 0;
        Object vIn = input;
        if (null != vIn && vd.hasValues()) {
            vIn = vd.getValues().get(vIn.toString(), input);
        }
        Boolean isTrue = Castors.me().castTo(vIn, Boolean.class);
        if (null != isTrue && isTrue.booleanValue()) {
            bi = 1;
        }
        WnEnumOptionItem[] options = vd.getOptions();
        if (null != options && options.length >= 2) {
            return options[bi].getValue();
        }
        return bi == 1;
    }

    // @Override
    // public String toStr(WnValue vd, Object val) {
    // int bi = 0;
    // if (Castors.me().castTo(val, Boolean.class)) {
    // bi = 1;
    // }
    // WnEnumOptionItem[] options = vd.getOptions();
    // if (null != options && options.length >= 2) {
    // return options[bi].getText();
    // }
    // return bi == 0 ? "false" : "true";
    // }

}