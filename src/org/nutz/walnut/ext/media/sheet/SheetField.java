package org.nutz.walnut.ext.media.sheet;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.lang.Each;
import org.nutz.lang.Lang;
import org.nutz.lang.Mirror;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.mapl.Mapl;
import org.nutz.walnut.api.err.Er;

public class SheetField {

    public String title;

    public String[] keys;

    public SheetFieldType type;

    /**
     * 针对不同的类型，可能有不同的配置信息。譬如
     * <ul>
     * <li>日期时间，这个字段就是格式化字符串等
     * <li>数组，这个就是一个数组元素对象的键，如果不设置，就用整个对象
     * <li>映射，这个就是一个 Map 存储映射
     * </ul>
     */
    public Object arg;

    public String getKey() {
        if (Strings.isBlank(title)) {
            for (String key : keys) {
                return key;
            }
            throw Er.create("e.sheet.noKey");
        }
        return this.title;
    }

    public Object getValue(NutMap obj) {
        // 首先获取值
        Object val = null;
        for (String key : keys) {
            val = Mapl.cell(obj, key);
            if (null != val)
                break;
        }

        // 布尔
        if (SheetFieldType.BOOLEAN == this.type) {
            // 没有参数的话，转成 boolean
            if (null == this.arg) {
                return null == val ? Boolean.FALSE : Castors.me().castTo(val, Boolean.class);
            }

            // 根据参数转换
            String[] ss = (String[]) this.arg;

            // ->Yes/No
            if ("->".equals(ss[0])) {
                boolean b = null == val ? Boolean.FALSE : Castors.me().castTo(val, Boolean.class);
                return b ? ss[1] : ss[2];
            }
            // <-Yes/No
            else {
                String s = Castors.me().castToString(val);
                return ss[1].equalsIgnoreCase(s) ? true : false;
            }
        }

        // 字符串
        if (SheetFieldType.STR == this.type) {
            return null == val ? this.arg : Castors.me().castToString(val);
        }

        // 整数
        if (SheetFieldType.INT == this.type) {
            return null == val ? this.arg : Castors.me().castTo(val, Integer.class);
        }

        // 长整数
        if (SheetFieldType.LONG == this.type) {
            return null == val ? this.arg : Castors.me().castTo(val, Long.class);
        }

        // 浮点
        if (SheetFieldType.FLOAT == this.type) {
            return null == val ? this.arg : Castors.me().castTo(val, Float.class);
        }

        // 双精度浮点
        if (SheetFieldType.DOUBLE == this.type) {
            return null == val ? this.arg : Castors.me().castTo(val, Double.class);
        }

        // 空值
        if (null == val)
            return null;

        // 映射
        if (SheetFieldType.MAPPING == this.type) {
            NutMap map = (NutMap) arg;
            String vk = val.toString();
            return map.get(vk, val);
        }

        // 数组？
        if (SheetFieldType.ARRAY == this.type) {
            String key = Strings.sBlank(arg, null);
            // 采用整个对象
            if (null == key) {
                // 数组
                if (val.getClass().isArray()) {
                    return Strings.join(", ", (Object[]) val);
                }
                // 容器
                if (val instanceof Collection<?>) {
                    return Strings.join(", ", (Collection<?>) val);
                }
                // 就是一个普通值咯, 那么变成数组
                String str = Castors.me().castToString(val);
                String ss[] = Strings.splitIgnoreBlank(str);

                // 如果都是数字，自动变一下数字
                Object[] oo = (Object[]) Array.newInstance(Object.class, ss.length);
                for (int i = 0; i < ss.length; i++) {
                    String s = ss[i];
                    if (s.matches("^([0-9]+)$")) {
                        oo[i] = Integer.parseInt(ss[i]);
                    } else {
                        oo[i] = s;
                    }
                }
                return oo;
            }
            // 获取对象的一个值
            else {
                String[] vals = new String[Lang.eleSize(val)];
                Lang.each(val, new Each<Map<String, Object>>() {
                    public void invoke(int index, Map<String, Object> ele, int length) {
                        NutMap map = NutMap.WRAP(ele);
                        Object val = Mapl.cell(map, key);
                        vals[index] = null == val ? "--" : val.toString();
                    }
                });
                return Strings.join(", ", vals);
            }
        }

        // 日期
        if (SheetFieldType.DATE == this.type) {
            String fmt = Strings.sBlank(arg, "yyyy-MM-dd HH:mm:ss");
            Date d = Castors.me().castTo(val, Date.class);
            return Times.format(fmt, d);
        }

        // 普通值，就直接使用
        Mirror<?> mi = Mirror.me(val);
        if (mi.isSimple()) {
            return val;
        }

        // 其他值变字符串
        return Castors.me().castToString(val);
    }

}
