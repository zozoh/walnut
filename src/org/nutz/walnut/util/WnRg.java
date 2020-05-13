package org.nutz.walnut.util;

import java.util.regex.Matcher;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.Regex;

public abstract class WnRg {

    private static String L = "([\\[(])";
    private static String R = "([\\])])";
    private static String seg = "([ \t]*,[ \t]*)";
    private static String FMT = "%5$s%1$s(%3$s|(%3$s%4$s%3$s)|(%3$s%4$s)|(%4$s%3$s))%2$s%6$s";

    private static String __regex(String v) {
        return __regex(v, "^");
    }

    private static String __regex(String v, String prefix) {
        return String.format(FMT, L, R, v, seg, prefix, "$");
    }

    public static String any() {
        return __regex("(.+)");
    }

    public static String intRegion() {
        return __regex("([0-9]{1,9})");
    }

    public static String longRegion() {
        return __regex("([0-9]{9,})");
    }

    public static String floatRegion() {
        return __regex("([0-9]*[.][0-9]+)");
    }

    public static String dateRegion(String prefix) {
        return __regex("([0-9T/: -]{10,})", prefix);
    }

    /**
     * 如果范围字符串里面包含动态内容，展开它。如果甚至不是范围字符串，那么直接返回
     * 
     * @param s
     *            范围字符串
     * @return 展开后的范围字符串。
     * 
     * @see org.nutz.walnut.util.Wn#fmt_str_macro(String)
     */
    public static String extend_rg_macro(String s) {
        Matcher m = Regex.getPattern(any()).matcher(s);
        if (m.find()) {
            String s_match = m.group(2);
            int pos = s_match.indexOf(',');
            String[] ss;
            // 可拆分
            if (pos >= 0) {
                ss = Lang.array(Strings.trim(s_match.substring(0, pos)),
                                Strings.trim(s_match.substring(pos + 1)));

            }
            // 就一个
            else {
                ss = Lang.array(Strings.trim(s_match));
            }
            // 对每个范围项目，深入展开内容
            for (int i = 0; i < ss.length; i++) {
                Object vr = Wn.fmt_str_macro(ss[i]);
                ss[i] = Castors.me().castToString(vr);
            }
            s = m.group(1) + Lang.concat(",", ss) + m.group(14);
        }
        return s;
    }

}
