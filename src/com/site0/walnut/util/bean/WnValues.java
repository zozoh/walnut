package com.site0.walnut.util.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.nutz.lang.util.NutBean;

import com.site0.walnut.util.Wtime;
import com.site0.walnut.util.bean.val.WnAMSValueAdaptor;
import com.site0.walnut.util.bean.val.WnArrayValueAdaptor;
import com.site0.walnut.util.bean.val.WnBooleanValueAdaptor;
import com.site0.walnut.util.bean.val.WnDateTextValueAdaptor;
import com.site0.walnut.util.bean.val.WnDateTimeValueAdaptor;
import com.site0.walnut.util.bean.val.WnDateValueAdaptor;
import com.site0.walnut.util.bean.val.WnFloatValueAdaptor;
import com.site0.walnut.util.bean.val.WnIntegerValueAdaptor;
import com.site0.walnut.util.bean.val.WnObjectValueAdaptor;
import com.site0.walnut.util.bean.val.WnStringValueAdaptor;
import com.site0.walnut.util.bean.val.WnValueAdaptor;
import com.site0.walnut.util.bean.val.WnValueType;

public class WnValues {

    private static Map<WnValueType, WnValueAdaptor> map;

    static {
        map = new HashMap<>();
        map.put(WnValueType.Integer, new WnIntegerValueAdaptor());
        map.put(WnValueType.Float, new WnFloatValueAdaptor());
        map.put(WnValueType.String, new WnStringValueAdaptor());
        map.put(WnValueType.Array, new WnArrayValueAdaptor());
        map.put(WnValueType.Object, new WnObjectValueAdaptor());
        map.put(WnValueType.AMS, new WnAMSValueAdaptor());
        map.put(WnValueType.Date, new WnDateValueAdaptor());
        map.put(WnValueType.DateTime, new WnDateTimeValueAdaptor());
        map.put(WnValueType.DateText, new WnDateTextValueAdaptor());
        map.put(WnValueType.Boolean, new WnBooleanValueAdaptor());
    }

    public static Object toValue(WnValue fld, Object input, NutBean bean) {
        WnValueAdaptor wv = map.get(fld.getType());
        return wv.toValue(fld, input, bean);
    }

    public static Date parseDate(Object input, String datePrefix) {
        if (null == input) {
            return null;
        }
        if (null != datePrefix && (input instanceof String)) {
            String str = (String) input;
            // 如果输入的是一个整数，那么通常是一个带有前缀的天数
            int iN = 0;
            try {
                iN = Integer.parseInt(str);
            }
            catch (Exception e2) {}

            if (iN > 0) {
                String in2 = datePrefix + str;
                return Wtime.parseDate(in2);
            }
        }
        return Wtime.parseAnyDate(input);
    }
}
