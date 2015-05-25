package org.nutz.walnut.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;

/**
 * 解析命令参数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZParams {

    public String[] vals;

    NutMap map;

    private static boolean _match_bool_char(String key, String chars) {
        char[] cs = key.toCharArray();
        for (char c : cs)
            if (chars.indexOf(c) == -1)
                return false;
        return true;
    }

    /**
     * 解析传入的参数表
     * 
     * <pre>
     * 如果参数以 "-" 开头，则所谓名值对的键。
     * 如果后面接着一个 "-" 开头的参数，则认为当前项目是布尔
     * 当然，如果给入的参数 bools 匹配上了这个参数，也认为是布尔
     * </pre>
     * 
     * @param args
     *            参数表
     * @param bools
     *            描述了布尔值，如果以 "^" 开头，则是正则表达式，匹配的键作为布尔项<br>
     *            否则认为是一个普通的布尔字符串
     * @return 参数表
     */
    public static ZParams parse(String[] args, String bools) {
        ZParams params = new ZParams();
        List<String> list = new ArrayList<String>(args.length);
        params.map = new NutMap();
        if (args.length > 0) {
            boolean is_define_bool_chars = false;
            Pattern bool_key = null;
            if (!Strings.isBlank(bools)) {
                if (bools.startsWith("^")) {
                    bool_key = Pattern.compile(bools);
                } else {
                    is_define_bool_chars = true;
                }
            }
            // 参数表 ...
            int i = 0;
            for (; i < args.length; i++) {
                String s = args[i];
                // 键值
                if (s.matches("^-[a-zA-Z_].*$")) {
                    String key = s.substring(1);
                    // 是否是布尔值表
                    if (is_define_bool_chars && _match_bool_char(key, bools)) {
                        char[] cs = key.toCharArray();
                        for (char c : cs) {
                            params.map.put("" + c, true);
                        }
                    }
                    // 键就是布尔值
                    else if (null != bool_key && bool_key.matcher(key).matches()) {
                        params.map.put(key, true);
                    }
                    // 木有后面的值了，那么作为 boolean
                    else if (i >= args.length - 1) {
                        params.map.put(key, true);
                        break;
                    }
                    // 如果有值 ...
                    else {
                        s = args[i+1];
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

    public boolean is(String key) {
        return map.getBoolean(key, false);
    }

    public boolean is(String key, boolean dft) {
        return map.getBoolean(key, dft);
    }

    public boolean has(String key) {
        return map.containsKey(key);
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
        return Double.valueOf(key);
    }

}
