package org.nutz.walnut.util.bean.val;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.util.bean.WnValue;

public class WnObjectValueAdaptor implements WnValueAdaptor {

    @Override
    public Object toValue(WnValue vd, Object input) {
        if (null == input) {
            return vd.getDefaultAs();
        }
        NutMap map = Castors.me().castTo(input, NutMap.class);
        if (map.isEmpty() && null != vd.getEmptyAs()) {
            return vd.getEmptyAs();
        }
        return map;
    }

    @Override
    public String toStr(WnValue vd, Object val) {
        if (null == val) {
            return null;
        }
        NutMap map = Castors.me().castTo(val, NutMap.class);
        return Json.toJson(map);
    }

}
