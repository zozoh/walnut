package org.nutz.walnut.util.bean.val;

import org.nutz.castor.Castors;
import org.nutz.walnut.util.bean.WnEnumOptionItem;
import org.nutz.walnut.util.bean.WnValue;

public class WnBooleanValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        int bi = 0;
        if (Castors.me().castTo(input, Boolean.class)) {
            bi = 1;
        }
        WnEnumOptionItem[] options = vd.getOptions();
        if (null != options && options.length >= 2) {
            return options[bi].getValue();
        }
        return bi == 1;
    }

    @Override
    public String toStr(WnValue vd, Object val) {
        int bi = 0;
        if (Castors.me().castTo(val, Boolean.class)) {
            bi = 1;
        }
        WnEnumOptionItem[] options = vd.getOptions();
        if (null != options && options.length >= 2) {
            return options[bi].getText();
        }
        return bi == 0 ? "false" : "true";
    }

}