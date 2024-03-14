package com.site0.walnut.util.bean.val;

import org.nutz.castor.Castors;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.util.bean.WnValue;

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
        // 执行转换
        if (vd.hasMapping()) {
            return vd.getMapping().translate(map, vd.isMappingOnly());
        }
        return map;
    }

    // @Override
    // public String toStr(WnValue vd, Object val) {
    // if (null == val) {
    // return null;
    // }
    // NutMap map = Castors.me().castTo(val, NutMap.class);
    // return Json.toJson(map);
    // }

}
