package org.nutz.walnut.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.LinkedCharArray;

public abstract class WnStr {

    private static String __safe(String str, char[] removed, char[] encoded) {
        char[] cs = str.toCharArray();
        LinkedCharArray re = new LinkedCharArray(cs.length);

        // 为了加快速度，对输入参数排序
        if (null != removed)
            Arrays.sort(removed);
        if (null != encoded)
            Arrays.sort(encoded);

        // 循环替换
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            // 移除
            if (null != removed && Arrays.binarySearch(removed, c) >= 0) {
                continue;
            }
            // 编码
            if (null != encoded && Arrays.binarySearch(encoded, c) >= 0) {
                re.push(String.format("%%%02X", (int) c).toUpperCase());
            }
            // 普通字符
            else {
                re.push(c);
            }
        }

        // 返回
        cs = re.toArray();
        return new String(cs);
    }

    /**
     * 对字符串内容进行逃逸处理。
     * 
     * <ul>
     * <li><code>'\r', '\n', ';'</code> 被删除
     * <li><code>'\'', '"', ';'</code> 被编码为 '%XX'
     * </ul>
     * 
     * @param str
     *            输入字符串
     * @return 逃逸后的字符串
     */
    public static String safe(String str) {
        if (str == null)
            return null;
        return __safe(str, new char[]{'\r', '\n', ';'}, new char[]{'\'', '"', '`'});
    }

    /**
     * 对字符串内容进行逃逸处理。
     * 
     * @param str
     *            输入字符串
     * @param removed
     *            要被移除的字符
     * @param encoded
     *            要被编码的字符, 即将字符变成 '%XX' 形式
     * @return 逃逸后的字符串
     */
    public static String safe(String str, char[] removed, char[] encoded) {
        if (null == str)
            return null;
        return __safe(str, removed, encoded);
    }

    /**
     * 对字符串进行逃逸处理。
     * 
     * @param str
     *            输入字符串
     * @param removed
     *            要被移除的字符
     * @param encoded
     *            要被编码的字符, 即将字符变成 '%XX' 形式
     * @return 逃逸后的字符串
     */
    public static String safe(String str, String removed, String encoded) {
        if (null == str)
            return null;
        return __safe(str,
                      removed == null ? null : removed.toCharArray(),
                      encoded == null ? null : encoded.toCharArray());
    }

    /**
     * 对字符串内容去掉空杯后进行逃逸处理。
     * 
     * <ul>
     * <li><code>'\r', '\n', ';'</code> 被删除
     * <li><code>'\'', '"', ';'</code> 被编码为 '%XX'
     * </ul>
     * 
     * @param str
     *            输入字符串
     * @return 逃逸后的字符串
     */
    public static String safeTrim(String str) {
        if (str == null)
            return null;
        return __safe(str.trim(), new char[]{'\r', '\n', ';'}, new char[]{'\'', '"', '`'});
    }

    /**
     * 对字符串内容去掉空杯后进行逃逸处理。
     * 
     * @param str
     *            输入字符串
     * @param removed
     *            要被移除的字符
     * @param encoded
     *            要被编码的字符, 即将字符变成 '%XX' 形式
     * @return 逃逸后的字符串
     */
    public static String safeTrim(String str, char[] removed, char[] encoded) {
        if (null == str)
            return null;
        return __safe(str.trim(), removed, encoded);
    }

    /**
     * 对字符串内容去掉空杯后进行逃逸处理。
     * 
     * @param str
     *            输入字符串
     * @param removed
     *            要被移除的字符
     * @param encoded
     *            要被编码的字符, 即将字符变成 '%XX' 形式
     * @return 逃逸后的字符串
     */
    public static String safeTrim(String str, String removed, String encoded) {
        if (null == str)
            return null;
        return __safe(str.trim(),
                      removed == null ? null : removed.toCharArray(),
                      encoded == null ? null : encoded.toCharArray());
    }

    /**
     * @param strs
     *            输入参数，半角逗号分隔的字符串也会被分隔
     * @return 抚平后的字符串列表
     */
    public static List<String> flatList(String... strs) {
        List<String> list = new LinkedList<>();
        for (String s : strs) {
            String[] ss = Strings.splitIgnoreBlank(s);
            if (ss.length == 1) {
                list.add(ss[0]);
            } else {
                for (String s2 : ss) {
                    list.add(s2);
                }
            }
        }
        return list;
    }

    /**
     * @param strs
     *            输入参数，半角逗号分隔的字符串也会被分隔
     * @return 抚平后的字符串数组
     */
    public static String[] flatArray(String... strs) {
        List<String> list = flatList(strs);
        return list.toArray(new String[list.size()]);
    }

}
