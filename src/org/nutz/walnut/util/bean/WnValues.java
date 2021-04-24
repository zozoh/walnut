package org.nutz.walnut.util.bean;

import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.util.bean.val.WnAMSValueAdaptor;
import org.nutz.walnut.util.bean.val.WnArrayValueAdaptor;
import org.nutz.walnut.util.bean.val.WnIntegerValueAdaptor;
import org.nutz.walnut.util.bean.val.WnObjectValueAdaptor;
import org.nutz.walnut.util.bean.val.WnStringValueAdaptor;
import org.nutz.walnut.util.bean.val.WnValueAdaptor;
import org.nutz.walnut.util.bean.val.WnValueType;

public class WnValues {

    private static Map<WnValueType, WnValueAdaptor> map;

    static {
        map = new HashMap<>();
        map.put(WnValueType.Integer, new WnIntegerValueAdaptor());
        map.put(WnValueType.String, new WnStringValueAdaptor());
        map.put(WnValueType.Array, new WnArrayValueAdaptor());
        map.put(WnValueType.Object, new WnObjectValueAdaptor());
        map.put(WnValueType.AMS, new WnAMSValueAdaptor());
    }

    public static Object toValue(WnValue fld, Object input) {
        WnValueAdaptor wv = map.get(fld.getType());
        return wv.toValue(fld, input);
    }

    public static String toStr(WnValue fld, Object val) {
        WnValueAdaptor wv = map.get(fld.getType());
        return wv.toStr(fld, val);
    }
}
