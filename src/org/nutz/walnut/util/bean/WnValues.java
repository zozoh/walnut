package org.nutz.walnut.util.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nutz.walnut.util.Wtime;
import org.nutz.walnut.util.bean.val.WnAMSValueAdaptor;
import org.nutz.walnut.util.bean.val.WnArrayValueAdaptor;
import org.nutz.walnut.util.bean.val.WnBooleanValueAdaptor;
import org.nutz.walnut.util.bean.val.WnDateTextValueAdaptor;
import org.nutz.walnut.util.bean.val.WnDateTimeValueAdaptor;
import org.nutz.walnut.util.bean.val.WnDateValueAdaptor;
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
        map.put(WnValueType.Date, new WnDateValueAdaptor());
        map.put(WnValueType.DateTime, new WnDateTimeValueAdaptor());
        map.put(WnValueType.DateText, new WnDateTextValueAdaptor());
        map.put(WnValueType.Boolean, new WnBooleanValueAdaptor());
    }

    public static Object toValue(WnValue fld, Object input) {
        WnValueAdaptor wv = map.get(fld.getType());
        return wv.toValue(fld, input);
    }


    public static Date parseDate(Object input, String datePrefix) {
        if(null == input) {
            return null;
        }
        if(null!=datePrefix) {
            String in2 = datePrefix + input.toString();
            return Wtime.parseDate(in2);
        }
        return Wtime.parseAnyDate(input);
    }
}
