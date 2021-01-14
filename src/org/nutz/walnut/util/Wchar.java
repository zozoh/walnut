package org.nutz.walnut.util;

import java.util.Arrays;

/**
 * 字符相关的帮助函数
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class Wchar {

    public static class EscapeTable {
        /**
         * 字符转义表: 奇数为，为字符字面量，偶数位为逃逸后的字符值
         * 
         * <pre>
         * [...,'\n',...,'\t'...]
         *        ^--- charCode 作为下标
         * </pre>
         */
        private char[] chars;

        /**
         * 字符转 int 时，需要偏移的值，因为 ASCII 表，前面基本是无用的，占地方
         */
        private int offset;

        /**
         * 根据输入字符获得转义字符
         * 
         * @param c
         *            输入字符
         * @return 表中转义的字符。 0 表示不在表中
         */
        public char get(char c) {
            int index = c - offset;
            if (index >= 0 && index < chars.length) {
                return chars[index];
            }
            return 0;
        }
    }

    /**
     * 根据输入的字符串，成对编制逃逸表，并且自动设置逃逸表的 offset
     * 
     * @param cs
     *            编制字符串逃逸表
     * @return 字符串逃逸表
     */
    public static EscapeTable buildEscapeTable(char[] cs) {
        // 先搜索一遍，最大的字符码
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        // 奇数为，为字符字面量，偶数位为逃逸后的字符值
        for (int i = 0; i < cs.length; i += 2) {
            int charCode = cs[i];
            max = Math.max(charCode, max);
            min = Math.min(charCode, min);
        }

        // 得到编码表的长度， min 自然就是偏移量
        int len = max - min + 1;
        char[] chars = new char[len];
        Arrays.fill(chars, (char) 0);
        for (int i = 0; i < cs.length; i+=2) {
            char c0 = cs[i];
            char c1 = cs[i + 1];
            int index = c0 - min;
            chars[index] = c1;
        }

        // 搞定返回
        EscapeTable table = new EscapeTable();
        table.chars = chars;
        table.offset = min;
        return table;
    }

    /**
     * @see #indexOf(char[], char, int)
     */
    public static int indexOf(char[] arr, char v) {
        if (null != arr)
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == v)
                    return i;
            }
        return -1;
    }

    /**
     * @param arr
     *            数组
     * @param v
     *            值
     * @param off
     *            从那个下标开始搜索(包含)
     * @return 第一个匹配元素的下标
     */
    public static int indexOf(char[] arr, char v, int off) {
        if (null != arr)
            for (int i = off; i < arr.length; i++) {
                if (arr[i] == v)
                    return i;
            }
        return -1;
    }

    /**
     * @param arr
     * @param v
     * @return 第一个匹配元素的下标
     */
    public static int lastIndexOf(char[] arr, char v) {
        if (null != arr)
            for (int i = arr.length - 1; i >= 0; i--) {
                if (arr[i] == v)
                    return i;
            }
        return -1;
    }

    /**
     * 判断一个长整数是否在数组中
     * 
     * @param arr
     *            数组
     * @param i
     *            长整数
     * @return 是否存在
     */
    public static boolean isIn(char[] arr, char i) {
        return indexOf(arr, i) >= 0;
    }

    /**
     * 不解释，你懂的
     */
    public static char[] array(char... is) {
        return is;
    }

    /**
     * 整合两个字符数组为一个数组 <b>这个方法在JDK5不可用!!<b/>
     * 
     * @param arr
     *            长整数数组
     * @param is
     *            变参
     * @return 新的整合过的数组
     */
    public static char[] concat(char[] arr, char... is) {
        if (null == arr || arr.length == 0)
            return is;
        if (null == is || is.length == 0)
            return arr;
        int length = arr.length + is.length;
        char[] re = new char[length];
        System.arraycopy(arr, 0, re, 0, arr.length);
        int i = arr.length;
        for (char c : is)
            re[i++] = c;
        return re;
    }

}
