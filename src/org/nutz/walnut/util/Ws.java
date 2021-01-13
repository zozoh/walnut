package org.nutz.walnut.util;

import java.util.LinkedList;
import java.util.List;

/**
 * 字符串帮助类
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Ws {

    /**
     * 统计某字符在给定字符串中出现的次数
     * 
     * @param cs
     *            字符串
     * @param c
     *            字符
     * @return 字符出现次数
     */
    public static int countChar(CharSequence cs, char c) {
        int re = 0;
        int len = cs.length();
        for (int i = 0; i < len; i++) {
            if (cs.charAt(i) == c) {
                re++;
            }
        }
        return re;
    }

    /**
     * 将字符串按半角逗号，拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s) {
        return splitIgnoreBlank(s, ",");
    }

    /**
     * 将字符串按半角逗号，拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @return 字符串列表
     */
    public static List<String> splitIgnoreBlanks(String s) {
        return splitIgnoreBlanks(s, ",");
    }

    /**
     * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @param regex
     *            正则式
     * @return 字符串数组
     */
    public static String[] splitIgnoreBlank(String s, String regex) {
        if (null == s)
            return null;
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st))
                continue;
            list.add(trim(st));
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * 根据一个正则式，将字符串拆分成数组，空元素将被忽略
     *
     * @param s
     *            字符串
     * @param regex
     *            正则式
     * @return 字符串列表
     */
    public static List<String> splitIgnoreBlanks(String s, String regex) {
        if (null == s)
            return null;
        String[] ss = s.split(regex);
        List<String> list = new LinkedList<String>();
        for (String st : ss) {
            if (isBlank(st))
                continue;
            list.add(trim(st));
        }
        return list;
    }

    /**
     * 去掉字符串前后空白字符。空白字符的定义由Character.isWhitespace来判断
     *
     * @param cs
     *            字符串
     * @return 去掉了前后空白字符的新字符串
     */
    public static String trim(CharSequence cs) {
        if (null == cs)
            return null;
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int l = 0;
        int last = length - 1;
        int r = last;
        for (; l < length; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        for (; r > l; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        if (l > r)
            return "";
        else if (l == 0 && r == last)
            return cs.toString();
        return cs.subSequence(l, r + 1).toString();
    }

    public static String trimStart(CharSequence cs) {
        if (null == cs)
            return null;
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int l = 0;
        for (; l < length; l++) {
            if (!Character.isWhitespace(cs.charAt(l)))
                break;
        }
        if ((length - 1) == l)
            return "";
        if (l > 0)
            return cs.subSequence(l, length).toString();
        return cs.toString();
    }

    public static String trimEnd(CharSequence cs) {
        if (null == cs)
            return null;
        int length = cs.length();
        if (length == 0)
            return cs.toString();
        int last = length - 1;
        int r = last;
        for (; r > 0; r--) {
            if (!Character.isWhitespace(cs.charAt(r)))
                break;
        }
        if (0 == r)
            return "";
        if (r == last)
            return cs.toString();
        return cs.subSequence(0, r + 1).toString();
    }

    /**
     * 如果此字符串为 null 或者为空串（""），则返回 true
     *
     * @param cs
     *            字符串
     * @return 如果此字符串为 null 或者为空，则返回 true
     */
    public static boolean isEmpty(CharSequence cs) {
        return null == cs || cs.length() == 0;
    }

    /**
     * 如果此字符串为 null 或者全为空白字符，则返回 true
     *
     * @param cs
     *            字符串
     * @return 如果此字符串为 null 或者全为空白字符，则返回 true
     */
    public static boolean isBlank(CharSequence cs) {
        if (null == cs)
            return true;
        int length = cs.length();
        for (int i = 0; i < length; i++) {
            if (!(Character.isWhitespace(cs.charAt(i))))
                return false;
        }
        return true;
    }

    /**
     * 复制字符串
     *
     * @param cs
     *            字符串
     * @param n
     *            数量
     * @return 新字符串
     */
    public static String repeat(CharSequence cs, int n) {
        if (isEmpty(cs) || n <= 0)
            return "";
        StringBuilder sb = new StringBuilder(cs.length() * n);
        for (int i = 0; i < n; i++)
            sb.append(cs);
        return sb.toString();
    }

    /**
     * 复制字符
     *
     * @param c
     *            字符
     * @param n
     *            数量
     * @return 新字符串
     */
    public static String repeat(char c, int n) {
        if (c == 0 || n < 1)
            return "";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++)
            sb.append(c);
        return sb.toString();
    }
}
