package org.nutz.walnut.util;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.DateRegion;
import org.nutz.lang.util.DoubleRegion;
import org.nutz.lang.util.FloatRegion;
import org.nutz.lang.util.IntRegion;
import org.nutz.lang.util.LongRegion;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.io.WnObj;

public class WnObjMatcher {

    private NutMap __map;

    public WnObjMatcher() {
        __map = new NutMap();
    }

    public WnObjMatcher setf(String fmt, Object... args) {
        return set(Lang.mapf(fmt, args));
    }

    public WnObjMatcher set(NutMap map) {
        __map.clear();
        for (Map.Entry<String, Object> en : map.entrySet()) {
            String key = en.getKey();
            Object val = en.getValue();

            // 空值表示没有
            if (null == val) {
                __map.put(key, null);
                continue;
            }

            // 数字类型
            if (val instanceof Number) {
                __map.put(key, val);
            }

            // 安装字符串来判断
            String s = val.toString();

            // 枚举
            if (s.startsWith("enum:")) {
                String[] ss = Strings.splitIgnoreBlank(s.substring("enum:".length()));
                __map.put(key, ss);
            }
            // 正则表达式
            else if (s.startsWith("^")) {
                Pattern p = Pattern.compile(s);
                __map.put(key, p);
            }
            // 正则表达式取反
            else if (s.startsWith("!^")) {
                __map.put(key, new NotPattern(s.substring(1)));
            }
            // rI : 整数区间
            else if (s.startsWith("rI:")) {
                __map.put(key, Region.Int(s.substring(3)));
            }
            // rL : 长整数区间
            else if (s.startsWith("rL:")) {
                __map.put(key, Region.Long(s.substring(3)));
            }
            // rD : 日期区间
            else if (s.startsWith("rD:")) {
                __map.put(key, Region.Date(s.substring(3)));
            }
            // rF : 浮点区间
            else if (s.startsWith("rF:")) {
                __map.put(key, Region.Float(s.substring(3)));
            }
            // rFF: 双精度浮点区间
            else if (s.startsWith("rFF:")) {
                __map.put(key, Region.Double(s.substring(4)));
            }
            // 其他的当做字符串
            else {
                __map.put(key, s);
            }
        }
        return this;
    }

    public boolean match(WnObj o) {
        for (Map.Entry<String, Object> en : __map.entrySet()) {
            String key = en.getKey();
            Object m = en.getValue();
            Object v = o.get(key);

            // 空值表示没有
            if (null == m) {
                if (null != v)
                    return false;
            }

            // 要的值没有喔
            if (null == v)
                return false;

            // 主要依靠字符串类型判断
            String vs = v.toString();

            // 数字类型
            if (m instanceof Number) {
                return m.toString().equals(vs);
            }

            // 枚举
            if (m.getClass().isArray()) {
                int len = Array.getLength(m);
                boolean found = false;
                for (int i = 0; i < len; i++) {
                    Object ele = Array.get(m, i);
                    if (ele.toString().equals(vs)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return false;
            }
            // 正则表达式
            else if (m instanceof Pattern) {
                if (!((Pattern) m).matcher(vs).find())
                    return false;
            }
            // 正则表达式取反
            else if (m instanceof NotPattern) {
                if (!((NotPattern) m).match(vs))
                    return false;
            }
            // rI : 整数区间
            else if (m instanceof IntRegion) {
                if (!((IntRegion) m).match(Castors.me().castTo(v, Integer.class)))
                    return false;
            }
            // rL : 长整数区间
            else if (m instanceof LongRegion) {
                if (!((LongRegion) m).match(Castors.me().castTo(v, Long.class)))
                    return false;
            }
            // rD : 日期区间
            else if (m instanceof DateRegion) {
                if (!((DateRegion) m).match(Castors.me().castTo(v, Date.class)))
                    return false;
            }
            // rF : 浮点区间
            else if (m instanceof FloatRegion) {
                if (!((FloatRegion) m).match(Castors.me().castTo(v, Float.class)))
                    return false;
            }
            // rFF: 双精度浮点区间
            else if (m instanceof DoubleRegion) {
                if (!((DoubleRegion) m).match(Castors.me().castTo(v, Double.class)))
                    return false;
            }
            // 其他的当做字符串
            else {
                if (!m.equals(vs))
                    return false;
            }
        }
        // 全部通过
        return true;
    }
}
