package com.site0.walnut.util;

import java.util.regex.Pattern;

/**
 * 正则工具（RM-NUTZ-13 替换 {@code 旧 Nutz Regex}）。
 * <p>
 * 不做全局 Pattern 缓存，调用点如需热点缓存请自行持有 Pattern。
 */
public class Wregex {

    public static void setCacheSize(int size) {}

    public static void clear() {}

    public static Pattern getPattern(String regex) {
        return Pattern.compile(regex);
    }

    public static boolean match(String regex, String value) {
        return getPattern(regex).matcher(value).find();
    }
}
