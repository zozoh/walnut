package org.nutz.walnut.util.bean.val;

import org.nutz.castor.Castors;
import org.nutz.walnut.util.bean.WnValue;

public class WnStringValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue fld, Object input) {
        if (null == input) {
            return fld.getDefaultAs();
        }
        if (fld.hasFormat()) {
            return String.format(fld.getFormat(), input);
        }
        return Castors.me().castToString(input);
    }

//    @Override
//    public String toStr(WnValue fld, Object val) {
//        if (fld.hasFormat()) {
//            return String.format(fld.getFormat(), val);
//        }
//        return Castors.me().castToString(val);
//    }

}
