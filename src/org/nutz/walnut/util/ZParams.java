package org.nutz.walnut.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;

/**
 * 解析命令参数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZParams implements Cloneable {

    private static final Pattern PARAM_KEY = Pattern.compile("^--?([a-zA-Z_]+[a-zA-Z0-9_-]*)$");

    public String[] vals;

    public String[] args;

    NutMap map;

    /**
     * @see #parse(String[], String, String)
     */
    public static ZParams parse(String[] args, String bools) {
        if (null == bools)
            return parse(args, null, null);

        if (bools.startsWith("^"))
            return parse(args, null, bools);

        return parse(args, bools, null);
    }

    /**
     * 解析传入的参数表
     * 
     * <pre>
     * 如果参数以 "-" 开头，则所谓名值对的键。
     * 如果后面接着一个 "-" 开头的参数，则认为当前项目是布尔
     * 当然，如果给入的参数 boolChars 或者 boolRegex 匹配上了这个参数，也认为是布尔
     * </pre>
     * 
     * @param args
     *            参数表
     * 
     * @param boolChars
     *            指明一个键的哪个字符是布尔值。 一个键如果全部内容都是布尔值，则分别记录。否则认为是一个普通键 <br>
     *            你可以直接给一个正则表达式来匹配 boolChar，但是你的正则表达式必须得有 group(1) 表示内容
     * 
     * @param boolRegex
     *            用一个正则表达式来描述哪些键（参数的整体）为布尔值
     * 
     * @return 参数表
     */
    public static ZParams parse(String[] args, String boolChars, String boolRegex) {
        ZParams params = new ZParams();
        List<String> list = new ArrayList<String>(args.length);
        params.args = args;
        params.map = new NutMap();
        if (args.length > 0) {

            // 预编译 boolRegex
            Pattern bool_key = null;
            if (!Strings.isBlank(boolRegex)) {
                bool_key = Pattern.compile(boolRegex);
            }

            // 预编译 boolChars，如果匹配这个正则表达式的参数，将被认为是一个布尔参数
            // 支持 -bish 这样的组合形式
            Pattern bool_char = null;
            if (!Strings.isBlank(boolChars)) {
                bool_char = Pattern.compile("^-([" + boolChars + "]+)$");
            }

            // 参数表 ...
            int i = 0;
            Matcher m;
            for (; i < args.length; i++) {
                String s = args[i];
                // boolChars
                // 是否是布尔值表
                if (null != bool_char) {
                    m = bool_char.matcher(s);
                    if (m.find()) {
                        char[] cs = m.group(m.groupCount()).toCharArray();
                        for (char c : cs) {
                            params.map.put("" + c, true);
                        }
                        continue;
                    }
                }

                // 键值
                m = PARAM_KEY.matcher(s);
                if (m.find()) {
                    String key = m.group(m.groupCount());
                    // 键就是布尔值
                    if (null != bool_key && bool_key.matcher(key).matches()) {
                        params.map.put(key, true);
                    }
                    // 木有后面的值了，那么作为 boolean
                    else if (i >= args.length - 1) {
                        params.map.put(key, true);
                        break;
                    }
                    // 如果有值 ...
                    else {
                        s = args[i + 1];
                        if (s.matches("^-[a-zA-Z_].*$")) {
                            params.map.put(key, true);
                            continue;
                        }
                        params.map.put(key, s);
                        // 跳过下一个值
                        i++;
                    }
                }
                // 嗯，是普通值 ...
                else {
                    list.add(s);
                }
            }
        }
        params.vals = list.toArray(new String[list.size()]);
        return params;
    }

    private ZParams() {}

    public ZParams clone() {
        ZParams params = new ZParams();
        params.args = Arrays.copyOf(this.args, this.args.length);
        params.vals = Arrays.copyOf(this.vals, this.vals.length);
        params.map = this.map.duplicate();
        return params;
    }

    public String[] subvals(int fromIndex, int len) {
        int max = vals.length - fromIndex;
        max = Math.min(max, len);
        if (max <= 0) {
            return new String[0];
        }
        String[] re = new String[max];
        System.arraycopy(vals, fromIndex, re, 0, max);
        return re;
    }

    public String[] subvals(int fromIndex) {
        return subvals(fromIndex, vals.length - fromIndex);
    }

    public String val(int index) {
        int i = index >= 0 ? index : vals.length + index;
        if (i < 0 || i >= vals.length)
            return null;
        return this.vals[i];
    }

    public String val(int index, String dft) {
        return Strings.sBlank(val(index), dft);
    }

    public int val_int(int index, int dft) {
        int i = index >= 0 ? index : vals.length + index;
        if (i < 0 || i >= vals.length)
            return dft;
        return Integer.parseInt(this.vals[i]);
    }

    public long val_long(int index, long dft) {
        int i = index >= 0 ? index : vals.length + index;
        if (i < 0 || i >= vals.length)
            return dft;
        return Long.parseLong(this.vals[i]);
    }

    public String val_check(int index) {
        String v = val(index);
        if (null == v) {
            throw Er.create("e.cmd.lack.param.vals", index);
        }
        return v;
    }

    public int val_check_int(int index) {
        return Integer.parseInt(val_check(index));
    }

    public long val_check_long(int index) {
        return Long.parseLong(val_check(index));
    }

    public Object explainVal(NutBean vars, int index, String dft) {
        String v = val(index, dft);
        if (null == v) {
            throw Er.create("e.cmd.lack.param.vals", index);
        }
        return Wn.explainObj(vars, v);
    }

    public String explainValAsString(NutBean vars, int index, String dft) {
        Object v = explainVal(vars, index, dft);
        return Castors.me().castToString(v);
    }

    public NutMap explainValAsMap(NutBean vars, int index, NutMap dft) {
        String json = val(index, null);
        NutMap map;
        if (Ws.isBlank(json)) {
            map = dft;
        } else {
            map = Wlang.map(json);
        }
        Object v = Wn.explainObj(vars, map);
        return Castors.me().castTo(v, NutMap.class);
    }

    public int explainValAsInt(NutBean vars, int index, String dft) {
        Object v = explainVal(vars, index, dft);
        return Castors.me().castTo(v, int.class);
    }

    public boolean explainValAsBool(NutBean vars, int index, String dft) {
        Object v = explainVal(vars, index, dft);
        return Castors.me().castTo(v, boolean.class);
    }

    public Object explainVal(NutBean vars, int index) {
        return explainVal(vars, index, null);
    }

    public String explainValAsString(NutBean vars, int index) {
        return explainValAsString(vars, index, null);
    }

    public NutMap explainValAsMap(NutBean vars, int index) {
        return explainValAsMap(vars, index, null);
    }

    public int explainValAsInt(NutBean vars, int index) {
        return explainValAsInt(vars, index, null);
    }

    public boolean explainValAsBool(NutBean vars, int index) {
        return explainValAsBool(vars, index, null);
    }

    public Object explain(NutBean vars, String name, Object dft) {
        Object v = map.get(name, dft);
        return Wn.explainObj(vars, v);
    }

    public String explainAsString(NutBean vars, String name, Object dft) {
        Object v = explain(vars, name, dft);
        return Castors.me().castToString(v);
    }

    public NutMap explainAsMap(NutBean vars, String name, NutMap dft) {
        NutMap map = this.getMap(name, dft);
        Object v = Wn.explainObj(vars, map);
        return Castors.me().castTo(v, NutMap.class);
    }

    public int explainAsInt(NutBean vars, String name, Object dft) {
        Object v = explain(vars, name, dft);
        return Castors.me().castTo(v, int.class);
    }

    public boolean explainAsBool(NutBean vars, String name, Object dft) {
        Object v = explain(vars, name, dft);
        return Castors.me().castTo(v, boolean.class);
    }

    public Object explain(NutBean vars, String name) {
        return explain(vars, name, null);
    }

    public String explainAsString(NutBean vars, String name) {
        return explainAsString(vars, name, null);
    }

    public NutMap explainAsMap(NutBean vars, String name) {
        return explainAsMap(vars, name, null);
    }

    public int explainAsInt(NutBean vars, String name) {
        return explainAsInt(vars, name, null);
    }

    public boolean explainAsBool(NutBean vars, String name) {
        return explainAsBool(vars, name, null);
    }

    public boolean is(boolean dft, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (null != v) {
                return Castors.me().castTo(v, boolean.class);
            }
        }
        return dft;
    }

    public boolean is(String... keys) {
        return is(false, keys);
    }

    public boolean is(String key) {
        return map.getBoolean(key, false);
    }

    public boolean is(String key, boolean dft) {
        return map.getBoolean(key, dft);
    }

    public void setv(String key, Object val) {
        map.setv(key, val);
    }

    public void setDftString(String key, String dft) {
        if (!this.hasString(key)) {
            map.put(key, dft);
        }
    }

    public boolean has(String key) {
        return map.has(key);
    }

    public boolean hasString(String key) {
        String val = this.get(key);
        return !Strings.isBlank(val) && !"true".equals(val);
    }

    public float getFloat(String key) {
        return map.getFloat(key, Float.NaN);
    }

    public float getFloat(String key, float dft) {
        return map.getFloat(key, dft);
    }

    public int getInt(String key) {
        return map.getInt(key, -1);
    }

    public int getInt(String key, int dft) {
        return map.getInt(key, dft);
    }

    public long getLong(String key) {
        return map.getLong(key, -1);
    }

    public long getLong(String key, long dft) {
        return map.getLong(key, dft);
    }

    public double getDouble(String key) {
        return map.getDouble(key, Double.NaN);
    }

    public double getDouble(String key, double dft) {
        return map.getDouble(key, dft);
    }

    public String get(String key) {
        return map.getString(key);
    }

    public String get(String key, String dft) {
        return map.getString(key, dft);
    }

    public String getString(String key) {
        return this.getString(key, "");
    }

    public String getString(String key, String dft) {
        Object val = map.get(key);
        if (null == val || val instanceof Boolean)
            return dft;
        return val.toString();
    }

    public String wrap(String key, String fmt) {
        return wrap(key, fmt, "");
    }

    public String wrap(String key, String fmt, String dft) {
        String val = this.get(key);
        if (Strings.isBlank(val)) {
            return dft;
        }
        return String.format(fmt, val);
    }

    public <T extends Enum<T>> T getEnum(String key, Class<T> classOfEnum) {
        return map.getEnum(key, classOfEnum);
    }

    public <T> T getAs(String key, Class<T> classOfT) {
        return map.getAs(key, classOfT);
    }

    public <T> T getAs(String key, Class<T> classOfT, T dft) {
        return map.getAs(key, classOfT, dft);
    }

    public NutMap getMap(String key) {
        return getMap(key, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public NutMap getMap(String key, NutMap dft) {
        Object val = map.get(key);
        if (null == val || "true".equals(val) || val instanceof Boolean)
            return dft;

        if (val instanceof Map)
            return NutMap.WRAP((Map) val);

        return Lang.map(val.toString());
    }

    public <T> List<T> getList(String key, Class<T> eleType) {
        return map.getList(key, eleType);
    }

    public String check(String key) {
        String v = get(key);
        if (Strings.isBlank(v)) {
            throw Er.create("e.cmd.lack.param", key);
        }
        return v;
    }

    public int checkInt(String key) {
        String v = get(key);
        if (Strings.isBlank(v)) {
            throw Er.create("e.cmd.lack.param.int", key);
        }
        return Integer.valueOf(v);
    }

    public long checkLong(String key) {
        String v = get(key);
        if (Strings.isBlank(v)) {
            throw Er.create("e.cmd.lack.param.long", key);
        }
        return Long.valueOf(v);
    }

    public float checkFloat(String key) {
        String v = get(key);
        if (Strings.isBlank(v)) {
            throw Er.create("e.cmd.lack.param.float", key);
        }
        return Float.valueOf(v);
    }

    public double checkDouble(String key) {
        String v = get(key);
        if (Strings.isBlank(v)) {
            throw Er.create("e.cmd.lack.param.double", key);
        }
        return Double.valueOf(v);
    }

    public NutMap map() {
        return map;
    }

    public String toJson(JsonFormat jf) {
        NutMap re = new NutMap();
        re.put("vals", vals);
        re.put("params", map);
        return Json.toJson(re, jf);
    }
}
